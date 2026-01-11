import enums.PaymentMode;
import enums.PaymentStatus;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public class PaymentController {

    // Store all payments by payment ID
    private Map<Integer, Payment> paymentsById;

    // Store payments by booking ID (if booking has payment reference)
    private Map<String, Payment> paymentsByBookingId;

    // Store payments by customer ID
    private Map<String, List<Payment>> paymentsByCustomerId;

    // Payment history for all transactions
    private List<Payment> paymentHistory;

    // Lock for payment operations
    private final Lock paymentLock = new ReentrantLock();

    // Counter for generating payment IDs
    private int paymentIdCounter = 10000;

    // Minimum payment amount
    private static final double MIN_PAYMENT_AMOUNT = 1.0;

    // Maximum payment amount (safety limit)
    private static final double MAX_PAYMENT_AMOUNT = 100000.0;

    public PaymentController() {
        this.paymentsById = new HashMap<>();
        this.paymentsByBookingId = new HashMap<>();
        this.paymentsByCustomerId = new HashMap<>();
        this.paymentHistory = new ArrayList<>();
    }

    /**
     * Process a payment for a booking
     */
    public Payment processPayment(String bookingId, PaymentMode paymentMode, double amount, String customerId) {
        paymentLock.lock();
        try {
            // Validate payment amount
            validatePaymentAmount(amount);

            // Check if payment already exists for this booking
            if (paymentsByBookingId.containsKey(bookingId)) {
                throw new RuntimeException("Payment already exists for booking: " + bookingId);
            }

            // Create payment object
            Payment payment = new Payment();
            payment.setPaymentId(generatePaymentId());
            payment.setPaymentMode(paymentMode);
            payment.setAmount(amount);
            payment.setPaymentDate(new Date());

            // Simulate payment processing (in real system, this would call payment gateway)
            boolean paymentSuccess = simulatePaymentProcessing(paymentMode, amount);

            if (paymentSuccess) {
                payment.setPaid(true);
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
            } else {
                payment.setPaid(false);
                payment.setPaymentStatus(PaymentStatus.FAILED);
            }

            // Store payment
            paymentsById.put(payment.getPaymentId(), payment);
            paymentsByBookingId.put(bookingId, payment);

            // Add to customer's payment history
            if (customerId != null) {
                List<Payment> customerPayments = paymentsByCustomerId.getOrDefault(customerId, new ArrayList<>());
                customerPayments.add(payment);
                paymentsByCustomerId.put(customerId, customerPayments);
            }

            // Add to payment history
            paymentHistory.add(payment);

            return payment;
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Process payment with retry mechanism
     */
    public Payment processPaymentWithRetry(String bookingId, PaymentMode paymentMode,
                                           double amount, String customerId, int maxRetries) {
        Payment payment = null;
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                payment = processPayment(bookingId, paymentMode, amount, customerId);
                if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                    return payment;
                }
                attempts++;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxRetries) {
                    throw new RuntimeException("Payment failed after " + maxRetries + " attempts: " + e.getMessage());
                }
            }
        }

        return payment;
    }

    /**
     * Simulate payment processing (replace with actual payment gateway integration)
     */
    private boolean simulatePaymentProcessing(PaymentMode paymentMode, double amount) {
        // Simulate network delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate payment success/failure (90% success rate for demo)
        // In real system, this would call actual payment gateway API
        Random random = new Random();
        return random.nextDouble() > 0.1; // 90% success rate
    }

    /**
     * Validate payment amount
     */
    private void validatePaymentAmount(double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        if (amount < MIN_PAYMENT_AMOUNT) {
            throw new RuntimeException("Payment amount must be at least ₹" + MIN_PAYMENT_AMOUNT);
        }

        if (amount > MAX_PAYMENT_AMOUNT) {
            throw new RuntimeException("Payment amount exceeds maximum limit of ₹" + MAX_PAYMENT_AMOUNT);
        }
    }

    /**
     * Generate unique payment ID
     */
    private int generatePaymentId() {
        return paymentIdCounter++;
    }

    /**
     * Get payment by payment ID
     */
    public Payment getPaymentById(int paymentId) {
        paymentLock.lock();
        try {
            return paymentsById.get(paymentId);
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get payment by booking ID
     */
    public Payment getPaymentByBookingId(String bookingId) {
        paymentLock.lock();
        try {
            return paymentsByBookingId.get(bookingId);
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Internal method to get customer payments without lock (assumes lock is already held)
     */
    private List<Payment> getCustomerPaymentsUnsafe(String customerId) {
        List<Payment> payments = paymentsByCustomerId.get(customerId);
        return payments != null ? new ArrayList<>(payments) : new ArrayList<>();
    }

    /**
     * Get all payments for a customer
     */
    public List<Payment> getCustomerPayments(String customerId) {
        paymentLock.lock();
        try {
            return getCustomerPaymentsUnsafe(customerId);
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get successful payments for a customer
     */
    public List<Payment> getSuccessfulCustomerPayments(String customerId) {
        paymentLock.lock();
        try {
            List<Payment> allPayments = getCustomerPaymentsUnsafe(customerId);
            return allPayments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                    .collect(Collectors.toList());
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get failed payments for a customer
     */
    public List<Payment> getFailedCustomerPayments(String customerId) {
        paymentLock.lock();
        try {
            List<Payment> allPayments = getCustomerPaymentsUnsafe(customerId);
            return allPayments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.FAILED)
                    .collect(Collectors.toList());
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Process refund for a payment
     */
    public Payment processRefund(int paymentId, String reason) {
        paymentLock.lock();
        try {
            Payment payment = paymentsById.get(paymentId);
            if (payment == null) {
                throw new RuntimeException("Payment not found: " + paymentId);
            }

            if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
                throw new RuntimeException("Cannot refund payment that was not successful");
            }

            // Create refund payment (negative amount)
            Payment refund = new Payment();
            refund.setPaymentId(generatePaymentId());
            refund.setPaymentMode(payment.getPaymentMode());
            refund.setAmount(-payment.getAmount()); // Negative amount for refund
            refund.setPaymentDate(new Date());
            refund.setPaid(true);
            refund.setPaymentStatus(PaymentStatus.SUCCESS);

            // Store refund
            paymentsById.put(refund.getPaymentId(), refund);
            paymentHistory.add(refund);

            // Update original payment status (optional - you might want to keep it as SUCCESS)
            // payment.setPaymentStatus(PaymentStatus.REFUNDED); // If you add REFUNDED to enum

            return refund;
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Update payment status
     */
    public boolean updatePaymentStatus(int paymentId, PaymentStatus newStatus) {
        paymentLock.lock();
        try {
            Payment payment = paymentsById.get(paymentId);
            if (payment == null) {
                return false;
            }

            payment.setPaymentStatus(newStatus);
            payment.setPaid(newStatus == PaymentStatus.SUCCESS);

            return true;
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Retry failed payment
     */
    public Payment retryPayment(int paymentId, PaymentMode paymentMode) {
        paymentLock.lock();
        try {
            Payment originalPayment = paymentsById.get(paymentId);
            if (originalPayment == null) {
                throw new RuntimeException("Payment not found: " + paymentId);
            }

            if (originalPayment.getPaymentStatus() != PaymentStatus.FAILED) {
                throw new RuntimeException("Can only retry failed payments");
            }

            // Create new payment attempt
            Payment retryPayment = new Payment();
            retryPayment.setPaymentId(generatePaymentId());
            retryPayment.setPaymentMode(paymentMode != null ? paymentMode : originalPayment.getPaymentMode());
            retryPayment.setAmount(originalPayment.getAmount());
            retryPayment.setPaymentDate(new Date());

            // Process payment
            boolean success = simulatePaymentProcessing(retryPayment.getPaymentMode(), retryPayment.getAmount());
            retryPayment.setPaid(success);
            retryPayment.setPaymentStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

            // Store retry payment
            paymentsById.put(retryPayment.getPaymentId(), retryPayment);
            paymentHistory.add(retryPayment);

            return retryPayment;
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get payment statistics for a customer
     */
    public Map<String, Object> getCustomerPaymentStats(String customerId) {
        paymentLock.lock();
        try {
            List<Payment> payments = getCustomerPaymentsUnsafe(customerId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPayments", payments.size());

            long successfulPayments = payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                    .count();
            stats.put("successfulPayments", successfulPayments);

            long failedPayments = payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.FAILED)
                    .count();
            stats.put("failedPayments", failedPayments);

            double totalAmount = payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                    .mapToDouble(Payment::getAmount)
                    .sum();
            stats.put("totalAmount", totalAmount);

            double averageAmount = successfulPayments > 0 ? totalAmount / successfulPayments : 0.0;
            stats.put("averageAmount", averageAmount);

            return stats;
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get payments by payment mode
     */
    public List<Payment> getPaymentsByMode(PaymentMode paymentMode) {
        paymentLock.lock();
        try {
            return paymentHistory.stream()
                    .filter(p -> p.getPaymentMode() == paymentMode)
                    .collect(Collectors.toList());
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get payments by status
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        paymentLock.lock();
        try {
            return paymentHistory.stream()
                    .filter(p -> p.getPaymentStatus() == status)
                    .collect(Collectors.toList());
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get payments within date range
     */
    public List<Payment> getPaymentsByDateRange(Date startDate, Date endDate) {
        paymentLock.lock();
        try {
            return paymentHistory.stream()
                    .filter(p -> p.getPaymentDate() != null &&
                            !p.getPaymentDate().before(startDate) &&
                            !p.getPaymentDate().after(endDate))
                    .collect(Collectors.toList());
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get all payment history
     */
    public List<Payment> getAllPayments() {
        paymentLock.lock();
        try {
            return new ArrayList<>(paymentHistory);
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get total revenue (sum of all successful payments)
     */
    public double getTotalRevenue() {
        paymentLock.lock();
        try {
            return paymentHistory.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS && p.getAmount() > 0)
                    .mapToDouble(Payment::getAmount)
                    .sum();
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get total refunds (sum of all refund payments)
     */
    public double getTotalRefunds() {
        paymentLock.lock();
        try {
            return Math.abs(paymentHistory.stream()
                    .filter(p -> p.getAmount() < 0)
                    .mapToDouble(Payment::getAmount)
                    .sum());
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Check if payment exists
     */
    public boolean paymentExists(int paymentId) {
        paymentLock.lock();
        try {
            return paymentsById.containsKey(paymentId);
        } finally {
            paymentLock.unlock();
        }
    }

    /**
     * Get payment details as string
     */
    public String getPaymentDetails(int paymentId) {
        Payment payment = getPaymentById(paymentId);
        if (payment == null) {
            return "Payment not found";
        }

        StringBuilder details = new StringBuilder();
        details.append("Payment ID: ").append(payment.getPaymentId()).append("\n");
        details.append("Amount: ₹").append(Math.abs(payment.getAmount())).append("\n");
        details.append("Payment Mode: ").append(payment.getPaymentMode()).append("\n");
        details.append("Status: ").append(payment.getPaymentStatus()).append("\n");
        details.append("Date: ").append(payment.getPaymentDate()).append("\n");
        details.append("Paid: ").append(payment.isPaid()).append("\n");

        if (payment.getAmount() < 0) {
            details.append("Type: REFUND\n");
        }

        return details.toString();
    }

    /**
     * Get payment receipt as string
     */
    public String generateReceipt(int paymentId) {
        Payment payment = getPaymentById(paymentId);
        if (payment == null) {
            return "Payment not found";
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            return "Cannot generate receipt for unsuccessful payment";
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("================================\n");
        receipt.append("      PAYMENT RECEIPT\n");
        receipt.append("================================\n");
        receipt.append("Payment ID: ").append(payment.getPaymentId()).append("\n");
        receipt.append("Amount: ₹").append(payment.getAmount()).append("\n");
        receipt.append("Payment Mode: ").append(payment.getPaymentMode()).append("\n");
        receipt.append("Status: ").append(payment.getPaymentStatus()).append("\n");
        receipt.append("Date: ").append(payment.getPaymentDate()).append("\n");
        receipt.append("================================\n");
        receipt.append("Thank you for your payment!\n");

        return receipt.toString();
    }
}