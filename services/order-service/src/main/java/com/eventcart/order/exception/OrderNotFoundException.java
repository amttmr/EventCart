package com.eventcart.order.exception;

/**
 * Exception thrown when order-service cannot find a requested order.
 */
public class OrderNotFoundException extends RuntimeException {
    /**
     * Creates an order-not-found exception.
     *
     * @param message explanation of the missing order
     */
    public OrderNotFoundException(String message) {
        super(message);
    }
}
