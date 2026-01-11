import enums.City;
import enums.PaymentMode;
import enums.SeatCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== Ticket Booking System ===\n");
        
        // Initialize controllers
        MovieController movieController = new MovieController();
        TheatreController theatreController = new TheatreController();
        CustomerController customerController = new CustomerController();
        BookingController bookingController = new BookingController();
        PaymentController paymentController = new PaymentController();
        
        // Link controllers
        bookingController.setPaymentController(paymentController);
        
        // Demo: Setup movies
        System.out.println("1. Setting up movies...");
        Movie movie1 = new Movie();
        movie1.setMovieId(1);
        movie1.setTitle("Avengers: Endgame");
        movie1.setGenre("Action");
        movie1.setLanguage("English");
        movie1.setDurationInMinutes(180);
        movieController.addMovie(movie1, City.BANGALORE);
        
        Movie movie2 = new Movie();
        movie2.setMovieId(2);
        movie2.setTitle("Inception");
        movie2.setGenre("Sci-Fi");
        movie2.setLanguage("English");
        movie2.setDurationInMinutes(148);
        movieController.addMovie(movie2, City.BANGALORE);
        
        System.out.println("   ✓ Movies added\n");
        
        // Demo: Setup theatres and screens
        System.out.println("2. Setting up theatres and screens...");
        
        // Theatre 1
        Theatre theatre1 = new Theatre();
        theatre1.setTheatreId(1);
        theatre1.setAddress("MG Road, Bangalore");
        theatre1.setCity(City.BANGALORE);
        
        // Screen 1 with seats
        Screen screen1 = new Screen();
        screen1.setScreenId(1);
        List<Seat> seats1 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Seat seat = new Seat();
            seat.setSeatNumber(i);
            seat.setAvailable(true);
            // Assign categories: first 5 premium, next 5 gold, next 5 silver, rest standard
            if (i <= 5) {
                seat.setSeatCategory(SeatCategory.PREMIUM);
            } else if (i <= 10) {
                seat.setSeatCategory(SeatCategory.GOLD);
            } else if (i <= 15) {
                seat.setSeatCategory(SeatCategory.SILVER);
            } else {
                seat.setSeatCategory(SeatCategory.STANDARD);
            }
            seats1.add(seat);
        }
        screen1.setSeats(seats1);
        theatre1.getScreens().add(screen1);
        
        // Create shows
        Show show1 = new Show(1, movie1, screen1, 1800); // 6:00 PM
        Show show2 = new Show(2, movie1, screen1, 2100); // 9:00 PM
        theatre1.getShows().add(show1);
        theatre1.getShows().add(show2);
        
        theatreController.addTheatre(theatre1, City.BANGALORE);
        System.out.println("   ✓ Theatre and screens setup complete\n");
        
        // Demo: Register customer
        System.out.println("3. Registering customer...");
        Customer customer = customerController.registerCustomer(
            "John Doe",
            "john.doe@example.com",
            "9876543210",
            "123 Main Street, Bangalore",
            "password123"
        );
        System.out.println("   ✓ Customer registered: " + customer.getCustomerId());
        System.out.println("   Name: " + customer.getCustomerName());
        System.out.println("   Email: " + customer.getCustomerEmail() + "\n");
        
        // Demo: Browse movies
        System.out.println("4. Browsing movies in Bangalore...");
        List<Movie> movies = movieController.getMoviesByCity(City.BANGALORE);
        for (Movie movie : movies) {
            System.out.println("   - " + movie.getTitle() + " (" + movie.getGenre() + ")");
        }
        System.out.println();
        
        // Demo: Get shows for a movie
        System.out.println("5. Getting shows for " + movie1.getTitle() + "...");
        Map<Theatre, List<Show>> shows = theatreController.getAllShows(movie1, City.BANGALORE);
        for (Map.Entry<Theatre, List<Show>> entry : shows.entrySet()) {
            Theatre theatre = entry.getKey();
            System.out.println("   Theatre: " + theatre.getAddress());
            for (Show show : entry.getValue()) {
                System.out.println("     Show ID: " + show.getShowId() + 
                    ", Time: " + formatTime(show.getShowStartTime()));
            }
        }
        System.out.println();
        
        // Demo: Check available seats
        System.out.println("6. Checking available seats for Show 1...");
        List<Seat> availableSeats = bookingController.getAvailableSeats(show1);
        System.out.println("   Available seats: " + availableSeats.size());
        System.out.print("   Seat numbers: ");
        for (Seat seat : availableSeats) {
            System.out.print(seat.getSeatNumber() + "(" + seat.getSeatCategory() + ") ");
        }
        System.out.println("\n");
        
        // Demo: Book tickets
        System.out.println("7. Booking tickets...");
        List<Integer> selectedSeats = Arrays.asList(1, 2, 3); // Premium seats
        try {
            Booking booking = bookingController.createBooking(customer, show1, selectedSeats);
            System.out.println("   ✓ Booking created!");
            System.out.println("   Booking ID: " + booking.getBookingId());
            System.out.println("   Total Amount: ₹" + booking.getTotalAmount());
            System.out.println("   Status: " + booking.getStatus() + "\n");
            
            // Demo: Process payment
            System.out.println("8. Processing payment...");
            Booking confirmedBooking = bookingController.confirmBooking(
                booking.getBookingId(), 
                PaymentMode.UPIPAY
            );
            
            if (confirmedBooking.getPayment() != null) {
                System.out.println("   ✓ Payment processed!");
                System.out.println("   Payment ID: " + confirmedBooking.getPayment().getPaymentId());
                System.out.println("   Payment Status: " + confirmedBooking.getPayment().getPaymentStatus());
                System.out.println("   Payment Mode: " + confirmedBooking.getPayment().getPaymentMode() + "\n");
            }
            
            // Demo: View booking details
            System.out.println("9. Booking Details:");
            System.out.println(bookingController.getBookingDetails(booking.getBookingId()));
            
            // Demo: Customer bookings
            System.out.println("10. Customer's booking history:");
            List<Booking> customerBookings = customerController.getCustomerBookings(customer.getCustomerId());
            System.out.println("   Total bookings: " + customerBookings.size());
            
            // Demo: Payment statistics
            System.out.println("\n11. Payment Statistics:");
            Map<String, Object> stats = paymentController.getCustomerPaymentStats(customer.getCustomerId());
            System.out.println("   Total Payments: " + stats.get("totalPayments"));
            System.out.println("   Successful: " + stats.get("successfulPayments"));
            System.out.println("   Total Amount: ₹" + stats.get("totalAmount"));
            
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    /**
     * Format time from 24-hour format (e.g., 1800) to readable format (e.g., 6:00 PM)
     */
    private static String formatTime(int time24) {
        int hours = time24 / 100;
        int minutes = time24 % 100;
        String period = hours >= 12 ? "PM" : "AM";
        int displayHours = hours > 12 ? hours - 12 : (hours == 0 ? 12 : hours);
        return String.format("%d:%02d %s", displayHours, minutes, period);
    }
}
