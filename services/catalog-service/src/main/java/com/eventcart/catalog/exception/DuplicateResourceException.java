package com.eventcart.catalog.exception;

/**
 * Exception thrown when a create request violates a uniqueness rule.
 *
 * <p>The catalog service currently uses this for duplicate product SKUs.</p>
 */
public class DuplicateResourceException extends RuntimeException {
    /**
     * Creates a duplicate-resource exception with a human-readable message.
     *
     * @param message explanation of the duplicate resource
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
