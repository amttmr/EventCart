package com.eventcart.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body used to update an existing product.
 *
 * @param name updated product display name
 * @param description updated product description
 * @param category updated category
 * @param price updated price
 * @param currency updated currency code
 * @param availableQuantity updated available quantity
 * @param tags updated searchable tags
 * @param active updated active flag
 */
public record UpdateProductRequest(
        @Schema(description = "Updated product display name", example = "Mechanical Keyboard Pro")
        @NotBlank(message = "Product name is required")
        String name,

        @Schema(description = "Updated product description", example = "Hot-swappable keyboard with RGB lighting and wireless mode")
        String description,

        @Schema(description = "Updated category", example = "Electronics")
        @NotBlank(message = "Category is required")
        String category,

        @Schema(description = "Updated product price", example = "7999.00")
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @Schema(description = "Updated currency code", example = "INR")
        @NotBlank(message = "Currency is required")
        String currency,

        @Schema(description = "Updated quantity available for sale", example = "30")
        @Min(value = 0, message = "Available quantity cannot be negative")
        int availableQuantity,

        @Schema(description = "Updated searchable product tags", example = "[\"keyboard\", \"gaming\", \"wireless\"]")
        List<String> tags,

        @Schema(description = "Whether the product is active and can be added to cart", example = "true")
        boolean active
) {
}
