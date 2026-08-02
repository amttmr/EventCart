package com.eventcart.common.events;

/**
 * Kafka event published when inventory-service cannot reserve stock.
 *
 * @param metadata common event metadata used for traceability and versioning
 * @param orderId order ID whose reservation failed
 * @param customerId customer that placed the order
 * @param reason human-readable failure reason
 */
public record InventoryReservationFailedEvent(
        EventMetadata metadata,
        String orderId,
        String customerId,
        String reason
) {
    public static final String EVENT_TYPE = "inventory.reservation.failed";
    public static final int VERSION = 1;
}
