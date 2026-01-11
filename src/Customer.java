import java.util.List;

public class Customer {
    String customerName;
    String customerEmail;
    String customerId;
    String phoneNumber;
    String address;
    String password;

    public List<Booking> getBookingList() {
        return bookingList;
    }

    public void setBookingList(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    List<Booking> bookingList;


    public Customer(String customerName, String customerEmail, String customerId, String phoneNumber, String address, String password) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerId = customerId;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.password = password;
    }
}
