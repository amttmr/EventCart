package com.eventcart.inventory.exception;

/**
 * Exception thrown when an inventory reservation cannot be found.
 */
public class InventoryReservationNotFoundException extends RuntimeException {
    /**
     * Creates an inventory-reservation-not-found exception.
     *
     * @param message explanation of the missing reservation
     */
    public InventoryReservationNotFoundException(String message) {
        super(message);
    }
}
