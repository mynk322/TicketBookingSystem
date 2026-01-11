import enums.BookingStatus;
import enums.PaymentMode;
import enums.PaymentStatus;
import enums.SeatCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BookingController {
    
    // Reference to PaymentController for payment processing
    private PaymentController paymentController;

    private Map<String, Booking> allBookings;
    private Map<Integer, List<Booking>> showBookings;

    // Lock for overall booking operations
    private final Lock bookingLock = new ReentrantLock();

    // Per-show locks to allow concurrent bookings for different shows
    private final Map<Integer, Lock> showLocks = new HashMap<>();
    private final Lock showLocksMapLock = new ReentrantLock();

    public BookingController() {
        this.allBookings = new HashMap<>();
        this.showBookings = new HashMap<>();
    }
    
    /**
     * Set PaymentController for payment processing
     */
    public void setPaymentController(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    /**
     * Get or create a lock for a specific show
     */
    private Lock getShowLock(int showId) {
        showLocksMapLock.lock();
        try {
            return showLocks.computeIfAbsent(showId, k -> new ReentrantLock());
        } finally {
            showLocksMapLock.unlock();
        }
    }

    /**
     * Get available seats for a given show (thread-safe)
     */
    public List<Seat> getAvailableSeats(Show show) {
        Lock showLock = getShowLock(show.getShowId());
        showLock.lock();
        try {
            List<Seat> availableSeats = new ArrayList<>();
            List<Seat> allSeats = show.getScreen().getSeats();
            List<Integer> bookedSeatIds = show.getBookedSeatIds();

            for (Seat seat : allSeats) {
                if (!bookedSeatIds.contains(seat.getSeatNumber()) && seat.isAvailable()) {
                    availableSeats.add(seat);
                }
            }
            return availableSeats;
        } finally {
            showLock.unlock();
        }
    }

    /**
     * Check if seats are available for booking (thread-safe)
     */
    public boolean areSeatsAvailable(Show show, List<Integer> seatNumbers) {
        Lock showLock = getShowLock(show.getShowId());
        showLock.lock();
        try {
            List<Integer> bookedSeatIds = show.getBookedSeatIds();
            List<Seat> allSeats = show.getScreen().getSeats();

            for (Integer seatNumber : seatNumbers) {
                // Check if seat is already booked
                if (bookedSeatIds.contains(seatNumber)) {
                    return false;
                }

                // Check if seat exists and is available
                boolean seatExists = false;
                for (Seat seat : allSeats) {
                    if (seat.getSeatNumber() == seatNumber) {
                        seatExists = true;
                        if (!seat.isAvailable()) {
                            return false;
                        }
                        break;
                    }
                }
                if (!seatExists) {
                    return false;
                }
            }
            return true;
        } finally {
            showLock.unlock();
        }
    }

    /**
     * Create a booking for a customer (thread-safe)
     */
    public Booking createBooking(Customer customer, Show show, List<Integer> seatNumbers) {
        Lock showLock = getShowLock(show.getShowId());
        showLock.lock();
        try {
            // Validate seats are available (double-check after acquiring lock)
            if (!areSeatsAvailableUnsafe(show, seatNumbers)) {
                throw new RuntimeException("Selected seats are not available");
            }

            // Create booking object
            Booking booking = new Booking();
            booking.setShow(show);
            booking.setCustomer(customer);

            // Get and set booked seats
            List<Seat> bookedSeats = new ArrayList<>();
            List<Seat> allSeats = show.getScreen().getSeats();
            for (Integer seatNumber : seatNumbers) {
                for (Seat seat : allSeats) {
                    if (seat.getSeatNumber() == seatNumber) {
                        bookedSeats.add(seat);
                        seat.setAvailable(false); // Mark seat as unavailable
                        break;
                    }
                }
            }
            booking.setBookedSeats(bookedSeats);

            // Calculate total amount using PriceCalculator
            double totalAmount = PriceCalculator.calculateTotal(bookedSeats);
            booking.setTotalAmount(totalAmount);

            // Generate booking ID
            booking.generateBookingId();
            
            // Set initial booking status
            booking.setStatus(BookingStatus.CONFIRMED);

            // Store booking (need lock for this too)
            bookingLock.lock();
            try {
                allBookings.put(booking.getBookingId(), booking);

                // Add to show bookings
                List<Booking> bookingsForShow = showBookings.getOrDefault(show.getShowId(), new ArrayList<>());
                bookingsForShow.add(booking);
                showBookings.put(show.getShowId(), bookingsForShow);
            } finally {
                bookingLock.unlock();
            }

            // Add booking to customer's booking list
            synchronized (customer) {
                if (customer.getBookingList() == null) {
                    customer.setBookingList(new ArrayList<>());
                }
                customer.getBookingList().add(booking);
            }

            // Mark seats as booked in the show (CRITICAL: must be atomic)
            show.getBookedSeatIds().addAll(seatNumbers);

            return booking;
        } finally {
            showLock.unlock();
        }
    }

    /**
     * Internal method to check availability without acquiring lock
     * (assumes lock is already held)
     */
    private boolean areSeatsAvailableUnsafe(Show show, List<Integer> seatNumbers) {
        List<Integer> bookedSeatIds = show.getBookedSeatIds();
        List<Seat> allSeats = show.getScreen().getSeats();

        for (Integer seatNumber : seatNumbers) {
            if (bookedSeatIds.contains(seatNumber)) {
                return false;
            }

            boolean seatExists = false;
            for (Seat seat : allSeats) {
                if (seat.getSeatNumber() == seatNumber) {
                    seatExists = true;
                    if (!seat.isAvailable()) {
                        return false;
                    }
                    break;
                }
            }
            if (!seatExists) {
                return false;
            }
        }
        return true;
    }


    /**
     * Confirm booking and process payment (thread-safe)
     */
    public Booking confirmBooking(String bookingId, PaymentMode paymentMode) {
        bookingLock.lock();
        try {
            Booking booking = allBookings.get(bookingId);
            if (booking == null) {
                throw new RuntimeException("Booking not found");
            }
            
            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new RuntimeException("Booking is not in a valid state for payment");
            }

            // Process payment using PaymentController if available
            Payment payment;
            if (paymentController != null) {
                String customerId = booking.getCustomer() != null ? booking.getCustomer().getCustomerId() : null;
                payment = paymentController.processPayment(bookingId, paymentMode, booking.getTotalAmount(), customerId);
            } else {
                // Fallback to direct payment processing
                payment = new Payment();
                payment.processPayment(paymentMode, booking.getTotalAmount());
            }
            
            booking.setPayment(payment);
            
            // Update booking status based on payment status
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                booking.setStatus(BookingStatus.CONFIRMED);
            } else {
                booking.setStatus(BookingStatus.CONFIRMED); // Keep as confirmed, payment can be retried
            }

            return booking;
        } finally {
            bookingLock.unlock();
        }
    }

    /**
     * Cancel a booking (thread-safe)
     */
    public boolean cancelBooking(String bookingId) {
        bookingLock.lock();
        try {
            Booking booking = allBookings.get(bookingId);
            if (booking == null) {
                return false;
            }

            Show show = booking.getShow();
            Lock showLock = getShowLock(show.getShowId());

            // Need to hold both locks to avoid deadlock
            // Always acquire locks in consistent order (showLock first, then bookingLock)
            // But we already have bookingLock, so we need to release and re-acquire
            bookingLock.unlock();

            showLock.lock();
            bookingLock.lock();
            try {
                // Re-check booking still exists
                booking = allBookings.get(bookingId);
                if (booking == null) {
                    return false;
                }

                List<Seat> bookedSeats = booking.getBookedSeats();

                // Remove seat numbers from booked list
                for (Seat seat : bookedSeats) {
                    show.getBookedSeatIds().remove(Integer.valueOf(seat.getSeatNumber()));
                    seat.setAvailable(true); // Mark seat as available again
                }

                // Remove from show bookings
                List<Booking> bookingsForShow = showBookings.get(show.getShowId());
                if (bookingsForShow != null) {
                    bookingsForShow.remove(booking);
                }

                // Remove from all bookings
                allBookings.remove(bookingId);

                // Remove from customer's booking list
                Customer customer = booking.getCustomer();
                if (customer != null && customer.getBookingList() != null) {
                    customer.getBookingList().remove(booking);
                }

                return true;
            } finally {
                showLock.unlock();
            }
        } finally {
            bookingLock.unlock();
        }
    }

    /**
     * Get booking by ID (thread-safe read)
     */
    public Booking getBooking(String bookingId) {
        bookingLock.lock();
        try {
            return allBookings.get(bookingId);
        } finally {
            bookingLock.unlock();
        }
    }

    /**
     * Get all bookings for a customer (thread-safe)
     */
    public List<Booking> getCustomerBookings(Customer customer) {
        synchronized (customer) {
            return customer.getBookingList() != null ?
                    new ArrayList<>(customer.getBookingList()) : new ArrayList<>();
        }
    }

    /**
     * Get all bookings for a show (thread-safe)
     */
    public List<Booking> getShowBookings(Show show) {
        Lock showLock = getShowLock(show.getShowId());
        showLock.lock();
        try {
            List<Booking> bookings = showBookings.getOrDefault(show.getShowId(), new ArrayList<>());
            return new ArrayList<>(bookings); // Return copy to avoid external modification
        } finally {
            showLock.unlock();
        }
    }

    /**
     * Get booking details as string
     */
    public String getBookingDetails(String bookingId) {
        Booking booking = getBooking(bookingId);
        if (booking == null) {
            return "Booking not found";
        }

        StringBuilder details = new StringBuilder();
        details.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        details.append("Movie: ").append(booking.getShow().getMovie().getTitle()).append("\n");
        details.append("Show Time: ").append(booking.getShow().getShowStartTime()).append("\n");
        details.append("Seats: ");
        for (Seat seat : booking.getBookedSeats()) {
            details.append(seat.getSeatNumber()).append(" ");
        }
        details.append("\n");
        details.append("Total Amount: ₹").append(booking.getTotalAmount()).append("\n");
        if (booking.getPayment() != null) {
            details.append("Payment Status: ").append(booking.getPayment().getPaymentStatus()).append("\n");
        }

        return details.toString();
    }
}