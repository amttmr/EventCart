package com.eventcart.order.exception;

/**
 * Exception thrown when a customer attempts to place an order with no cart items.
 */
public class EmptyCartException extends RuntimeException {
    /**
     * Creates an empty-cart exception.
     *
     * @param message explanation of why the order cannot be placed
     */
    public EmptyCartException(String message) {
        super(message);
    }
}
