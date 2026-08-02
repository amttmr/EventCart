package com.eventcart.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body used to create or update inventory stock for one product.
 *
 * @param sku product SKU snapshot
 * @param productName product display name snapshot
 * @param availableQuantity quantity available for new reservations
 */
public record UpsertInventoryItemRequest(
        @NotBlank(message = "SKU is required")
        String sku,

        @NotBlank(message = "Product name is required")
        String productName,

        @Min(value = 0, message = "Available quantity cannot be negative")
        int availableQuantity
) {
}
