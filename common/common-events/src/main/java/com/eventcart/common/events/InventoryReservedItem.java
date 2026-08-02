package com.eventcart.common.events;

/**
 * Product quantity reserved by inventory-service for an order.
 *
 * @param productId product ID whose stock was reserved
 * @param sku product SKU snapshot
 * @param quantity quantity reserved
 */
public record InventoryReservedItem(
        String productId,
        String sku,
        int quantity
) {
}
