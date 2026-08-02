package com.eventcart.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body used to create a product in the catalog.
 *
 * @param sku business-facing stock keeping unit
 * @param name product display name
 * @param description optional product description
 * @param category product category used for filtering and browsing
 * @param price product price
 * @param currency currency code for the price
 * @param availableQuantity current available quantity
 * @param tags searchable product tags
 */
public record CreateProductRequest(
        @Schema(description = "Business-facing stock keeping unit", example = "SKU-1001")
        @NotBlank(message = "SKU is required")
        String sku,

        @Schema(description = "Product display name", example = "Mechanical Keyboard")
        @NotBlank(message = "Product name is required")
        String name,

        @Schema(description = "Optional product description", example = "Hot-swappable mechanical keyboard with RGB lighting")
        String description,

        @Schema(description = "Product category used for browsing and filtering", example = "Electronics")
        @NotBlank(message = "Category is required")
        String category,

        @Schema(description = "Product price", example = "6999.00")
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @Schema(description = "Currency code for the product price", example = "INR")
        @NotBlank(message = "Currency is required")
        String currency,

        @Schema(description = "Current quantity available for sale", example = "25")
        @Min(value = 0, message = "Available quantity cannot be negative")
        int availableQuantity,

        @Schema(description = "Searchable product tags", example = "[\"keyboard\", \"gaming\", \"rgb\"]")
        List<String> tags
) {
}
