package com.eventcart.cart.exception;

/**
 * Exception thrown when cart-service cannot successfully call catalog-service.
 */
public class CatalogServiceUnavailableException extends RuntimeException {
    /**
     * Creates a catalog-service unavailable exception.
     *
     * @param message explanation of the remote-service failure
     */
    public CatalogServiceUnavailableException(String message) {
        super(message);
    }

    /**
     * Creates a catalog-service unavailable exception with a root cause.
     *
     * @param message explanation of the remote-service failure
     * @param cause original client exception
     */
    public CatalogServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

