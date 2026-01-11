import enums.PaymentMode;
import enums.PaymentStatus;

import java.util.Date;

public class Payment {

    int paymentId;
    PaymentMode paymentMode;
    double amount;
    Date paymentDate;
    boolean isPaid;
    PaymentStatus paymentStatus;
    public void processPayment(PaymentMode paymentMode, double amount) {
        this.paymentId = generatePaymentId();
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.isPaid = true;
        this.paymentDate = new Date();
        this.paymentStatus = PaymentStatus.SUCCESS;
    }

    private int generatePaymentId() {
        return (int) (Math.random() * (44444 - 10001 + 1) + 10001);
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
