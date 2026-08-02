package com.eventcart.catalog.exception;

/**
 * Exception thrown when a requested catalog resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Creates a not-found exception with a human-readable message.
     *
     * @param message explanation of the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
