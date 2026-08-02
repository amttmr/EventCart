package com.eventcart.order.exception;

/**
 * Exception thrown when order-service cannot successfully call cart-service.
 */
public class CartServiceUnavailableException extends RuntimeException {
    /**
     * Creates a cart-service unavailable exception.
     *
     * @param message explanation of the remote-service failure
     */
    public CartServiceUnavailableException(String message) {
        super(message);
    }

    /**
     * Creates a cart-service unavailable exception with a root cause.
     *
     * @param message explanation of the remote-service failure
     * @param cause original client exception
     */
    public CartServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
