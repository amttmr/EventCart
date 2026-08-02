package com.eventcart.inventory.domain;

/**
 * Item quantity embedded inside an inventory reservation document.
 *
 * @param productId product ID whose stock was reserved
 * @param sku product SKU snapshot
 * @param quantity reserved quantity
 */
public record InventoryReservationItemDocument(
        String productId,
        String sku,
        int quantity
) {
}
