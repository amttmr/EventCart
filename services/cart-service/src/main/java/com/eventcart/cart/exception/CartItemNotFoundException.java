package com.eventcart.cart.exception;

/**
 * Exception thrown when a requested product is not present in the customer's cart.
 */
public class CartItemNotFoundException extends RuntimeException {
    /**
     * Creates a cart-item not-found exception.
     *
     * @param message explanation of the missing cart item
     */
    public CartItemNotFoundException(String message) {
        super(message);
    }
}

