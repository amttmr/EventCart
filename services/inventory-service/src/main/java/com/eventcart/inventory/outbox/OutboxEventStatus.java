package com.eventcart.inventory.outbox;

/**
 * Publication state for an inventory-service outbox event.
 */
public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
