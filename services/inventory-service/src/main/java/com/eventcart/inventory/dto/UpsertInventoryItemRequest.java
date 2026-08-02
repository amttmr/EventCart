package com.eventcart.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "Product SKU snapshot", example = "SKU-1001")
        @NotBlank(message = "SKU is required")
        String sku,

        @Schema(description = "Product display name snapshot", example = "Mechanical Keyboard")
        @NotBlank(message = "Product name is required")
        String productName,

        @Schema(description = "Quantity available for new reservations", example = "25")
        @Min(value = 0, message = "Available quantity cannot be negative")
        int availableQuantity
) {
}
