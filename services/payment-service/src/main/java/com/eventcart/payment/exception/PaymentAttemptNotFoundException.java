package com.eventcart.payment.exception;

/**
 * Exception thrown when a payment attempt cannot be found.
 */
public class PaymentAttemptNotFoundException extends RuntimeException {
    /**
     * Creates a payment-attempt-not-found exception.
     *
     * @param message explanation of the missing payment attempt
     */
    public PaymentAttemptNotFoundException(String message) {
        super(message);
    }
}
