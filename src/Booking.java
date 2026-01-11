import enums.BookingStatus;
import enums.PaymentMode;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    String bookingId;
    Show show;
    List<Seat> bookedSeats = new ArrayList<>();
    Payment payment;
    double totalAmount;
    BookingStatus status;
    Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void generateBookingId() {
        if (bookingId == null) {
            bookingId = "BMS" + (int) (Math.random() * (52222 - 10001 + 1) + 10001);
        }
    }
    
    public double calculateTotalAmount() {
        generateBookingId();
        // Fixed: Calculate based on booked seats, not all seats
        // This method should just generate ID, actual calculation done in BookingController
        return totalAmount;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public List<Seat> getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(List<Seat> bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
