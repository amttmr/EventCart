package com.eventcart.inventory.dto;

import java.time.Instant;

/**
 * Public representation of one inventory stock document.
 *
 * @param productId product ID
 * @param sku product SKU snapshot
 * @param productName product display name snapshot
 * @param availableQuantity quantity available for new reservations
 * @param reservedQuantity quantity already reserved for orders
 * @param version optimistic locking version maintained by MongoDB/Spring Data
 * @param updatedAt time when the inventory item was last modified
 */
public record InventoryItemResponse(
        String productId,
        String sku,
        String productName,
        int availableQuantity,
        int reservedQuantity,
        Long version,
        Instant updatedAt
) {
}
