package com.eventcart.payment.outbox;

/**
 * Publication state for a payment-service outbox event.
 */
public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
