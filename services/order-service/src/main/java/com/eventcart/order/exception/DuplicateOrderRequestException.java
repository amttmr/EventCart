package com.eventcart.order.exception;

/**
 * Exception thrown when an idempotency key is already processing another order request.
 */
public class DuplicateOrderRequestException extends RuntimeException {
    /**
     * Creates a duplicate-order-request exception.
     *
     * @param message explanation of the duplicate request
     */
    public DuplicateOrderRequestException(String message) {
        super(message);
    }
}
