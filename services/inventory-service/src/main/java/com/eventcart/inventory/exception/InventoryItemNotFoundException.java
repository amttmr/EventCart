package com.eventcart.inventory.exception;

/**
 * Exception thrown when an inventory stock document cannot be found.
 */
public class InventoryItemNotFoundException extends RuntimeException {
    /**
     * Creates an inventory-item-not-found exception.
     *
     * @param message explanation of the missing inventory item
     */
    public InventoryItemNotFoundException(String message) {
        super(message);
    }
}
