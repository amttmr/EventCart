package com.eventcart.inventory.dto;

/**
 * Public representation of one reserved product quantity.
 *
 * @param productId product ID whose stock was reserved
 * @param sku product SKU snapshot
 * @param quantity reserved quantity
 */
public record InventoryReservationItemResponse(
        String productId,
        String sku,
        int quantity
) {
}
