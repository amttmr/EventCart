package com.eventcart.order.outbox;

/**
 * Publication state for an outbox event.
 */
public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
