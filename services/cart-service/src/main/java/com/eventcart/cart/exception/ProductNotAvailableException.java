package com.eventcart.cart.exception;

/**
 * Exception thrown when a product cannot be added to a cart.
 *
 * <p>This can happen when catalog-service cannot find the product or reports it
 * as inactive.</p>
 */
public class ProductNotAvailableException extends RuntimeException {
    /**
     * Creates a product-not-available exception.
     *
     * @param message explanation of why the product cannot be used
     */
    public ProductNotAvailableException(String message) {
        super(message);
    }
}

