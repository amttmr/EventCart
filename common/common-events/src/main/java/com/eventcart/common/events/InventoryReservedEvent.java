package com.eventcart.common.events;

import java.util.List;

/**
 * Kafka event published when inventory-service successfully reserves stock.
 *
 * @param metadata common event metadata used for traceability and versioning
 * @param orderId order ID whose stock was reserved
 * @param customerId customer that placed the order
 * @param items reserved item quantities
 */
public record InventoryReservedEvent(
        EventMetadata metadata,
        String orderId,
        String customerId,
        List<InventoryReservedItem> items
) {
    public static final String EVENT_TYPE = "inventory.reserved";
    public static final int VERSION = 1;
}
