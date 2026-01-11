import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.regex.Pattern;

public class CustomerController {

    // Store all customers by customerId
    private Map<String, Customer> customersById;

    // Store customers by email for quick lookup
    private Map<String, Customer> customersByEmail;

    // Store customers by phone number
    private Map<String, Customer> customersByPhone;

    // Lock for customer operations
    private final Lock customerLock = new ReentrantLock();

    // Counter for generating customer IDs
    private int customerIdCounter = 1000;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Phone validation pattern (10 digits)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    public CustomerController() {
        this.customersById = new HashMap<>();
        this.customersByEmail = new HashMap<>();
        this.customersByPhone = new HashMap<>();
    }

    /**
     * Register a new customer
     */
    public Customer registerCustomer(String customerName, String customerEmail,
                                     String phoneNumber, String address, String password) {
        customerLock.lock();
        try {
            // Validate input
            validateCustomerData(customerName, customerEmail, phoneNumber, password);

            // Check if email already exists
            if (customersByEmail.containsKey(customerEmail.toLowerCase())) {
                throw new RuntimeException("Email already registered: " + customerEmail);
            }

            // Check if phone number already exists
            if (customersByPhone.containsKey(phoneNumber)) {
                throw new RuntimeException("Phone number already registered: " + phoneNumber);
            }

            // Generate unique customer ID
            String customerId = generateCustomerId();

            // Create new customer
            Customer customer = new Customer(customerName, customerEmail, customerId,
                    phoneNumber, address, password);
            customer.setBookingList(new ArrayList<>());

            // Store customer in all maps
            customersById.put(customerId, customer);
            customersByEmail.put(customerEmail.toLowerCase(), customer);
            customersByPhone.put(phoneNumber, customer);

            return customer;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Generate unique customer ID
     */
    private String generateCustomerId() {
        return "CUST" + (customerIdCounter++);
    }

    /**
     * Validate customer registration data
     */
    private void validateCustomerData(String customerName, String customerEmail,
                                      String phoneNumber, String password) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new RuntimeException("Customer name cannot be empty");
        }

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new RuntimeException("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(customerEmail).matches()) {
            throw new RuntimeException("Invalid email format: " + customerEmail);
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new RuntimeException("Phone number cannot be empty");
        }

        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new RuntimeException("Invalid phone number format. Must be 10 digits: " + phoneNumber);
        }

        if (password == null || password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }
    }

    /**
     * Customer login/authentication
     */
    public Customer login(String customerEmail, String password) {
        customerLock.lock();
        try {
            Customer customer = customersByEmail.get(customerEmail.toLowerCase());

            if (customer == null) {
                throw new RuntimeException("Customer not found with email: " + customerEmail);
            }

            if (!customer.getPassword().equals(password)) {
                throw new RuntimeException("Invalid password");
            }

            return customer;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Get customer by customer ID
     */
    public Customer getCustomerById(String customerId) {
        customerLock.lock();
        try {
            return customersById.get(customerId);
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Get customer by email
     */
    public Customer getCustomerByEmail(String customerEmail) {
        customerLock.lock();
        try {
            return customersByEmail.get(customerEmail.toLowerCase());
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Get customer by phone number
     */
    public Customer getCustomerByPhone(String phoneNumber) {
        customerLock.lock();
        try {
            return customersByPhone.get(phoneNumber);
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Update customer name
     */
    public boolean updateCustomerName(String customerId, String newName) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return false;
            }

            if (newName == null || newName.trim().isEmpty()) {
                throw new RuntimeException("Customer name cannot be empty");
            }

            customer.setCustomerName(newName);
            return true;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Update customer email
     */
    public boolean updateCustomerEmail(String customerId, String newEmail) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return false;
            }

            if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
                throw new RuntimeException("Invalid email format: " + newEmail);
            }

            // Check if new email already exists
            if (customersByEmail.containsKey(newEmail.toLowerCase()) &&
                    !customersByEmail.get(newEmail.toLowerCase()).getCustomerId().equals(customerId)) {
                throw new RuntimeException("Email already registered: " + newEmail);
            }

            // Remove old email mapping
            customersByEmail.remove(customer.getCustomerEmail().toLowerCase());

            // Update email
            customer.setCustomerEmail(newEmail);

            // Add new email mapping
            customersByEmail.put(newEmail.toLowerCase(), customer);

            return true;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Update customer phone number
     */
    public boolean updateCustomerPhone(String customerId, String newPhone) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return false;
            }

            if (!PHONE_PATTERN.matcher(newPhone).matches()) {
                throw new RuntimeException("Invalid phone number format. Must be 10 digits: " + newPhone);
            }

            // Check if new phone already exists
            if (customersByPhone.containsKey(newPhone) &&
                    !customersByPhone.get(newPhone).getCustomerId().equals(customerId)) {
                throw new RuntimeException("Phone number already registered: " + newPhone);
            }

            // Remove old phone mapping
            customersByPhone.remove(customer.getPhoneNumber());

            // Update phone
            customer.setPhoneNumber(newPhone);

            // Add new phone mapping
            customersByPhone.put(newPhone, customer);

            return true;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Update customer address
     */
    public boolean updateCustomerAddress(String customerId, String newAddress) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return false;
            }

            customer.setAddress(newAddress);
            return true;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Change customer password
     */
    public boolean changePassword(String customerId, String oldPassword, String newPassword) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return false;
            }

            // Verify old password
            if (!customer.getPassword().equals(oldPassword)) {
                throw new RuntimeException("Incorrect old password");
            }

            // Validate new password
            if (newPassword == null || newPassword.length() < 6) {
                throw new RuntimeException("New password must be at least 6 characters long");
            }

            customer.setPassword(newPassword);
            return true;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Update entire customer profile
     */
    public boolean updateCustomerProfile(String customerId, String customerName,
                                         String customerEmail, String phoneNumber, String address) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return false;
            }

            // Update name
            if (customerName != null && !customerName.trim().isEmpty()) {
                customer.setCustomerName(customerName);
            }

            // Update email (with validation)
            if (customerEmail != null && !customerEmail.equals(customer.getCustomerEmail())) {
                updateCustomerEmail(customerId, customerEmail);
            }

            // Update phone (with validation)
            if (phoneNumber != null && !phoneNumber.equals(customer.getPhoneNumber())) {
                updateCustomerPhone(customerId, phoneNumber);
            }

            // Update address
            if (address != null) {
                customer.setAddress(address);
            }

            return true;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Delete customer account
     */
    public boolean deleteCustomer(String customerId) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return false;
            }

            // Check if customer has active bookings
            if (customer.getBookingList() != null && !customer.getBookingList().isEmpty()) {
                throw new RuntimeException("Cannot delete customer with active bookings. Please cancel all bookings first.");
            }

            // Remove from all maps
            customersById.remove(customerId);
            customersByEmail.remove(customer.getCustomerEmail().toLowerCase());
            customersByPhone.remove(customer.getPhoneNumber());

            return true;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Get all bookings for a customer
     */
    public List<Booking> getCustomerBookings(String customerId) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return new ArrayList<>();
            }

            return customer.getBookingList() != null ?
                    new ArrayList<>(customer.getBookingList()) : new ArrayList<>();
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Get customer booking count
     */
    public int getCustomerBookingCount(String customerId) {
        customerLock.lock();
        try {
            Customer customer = customersById.get(customerId);
            if (customer == null) {
                return 0;
            }

            return customer.getBookingList() != null ? customer.getBookingList().size() : 0;
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Check if customer exists
     */
    public boolean customerExists(String customerId) {
        customerLock.lock();
        try {
            return customersById.containsKey(customerId);
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Check if email is already registered
     */
    public boolean isEmailRegistered(String email) {
        customerLock.lock();
        try {
            return customersByEmail.containsKey(email.toLowerCase());
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Check if phone number is already registered
     */
    public boolean isPhoneRegistered(String phoneNumber) {
        customerLock.lock();
        try {
            return customersByPhone.containsKey(phoneNumber);
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Get all customers (for admin purposes)
     */
    public List<Customer> getAllCustomers() {
        customerLock.lock();
        try {
            return new ArrayList<>(customersById.values());
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Get total number of registered customers
     */
    public int getTotalCustomers() {
        customerLock.lock();
        try {
            return customersById.size();
        } finally {
            customerLock.unlock();
        }
    }

    /**
     * Get customer details as string
     */
    public String getCustomerDetails(String customerId) {
        Customer customer = getCustomerById(customerId);
        if (customer == null) {
            return "Customer not found";
        }

        StringBuilder details = new StringBuilder();
        details.append("Customer ID: ").append(customer.getCustomerId()).append("\n");
        details.append("Name: ").append(customer.getCustomerName()).append("\n");
        details.append("Email: ").append(customer.getCustomerEmail()).append("\n");
        details.append("Phone: ").append(customer.getPhoneNumber()).append("\n");
        details.append("Address: ").append(customer.getAddress()).append("\n");
        details.append("Total Bookings: ").append(
                customer.getBookingList() != null ? customer.getBookingList().size() : 0
        ).append("\n");

        return details.toString();
    }
}