package com.eventcart.catalog.dto;

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
        @NotBlank(message = "SKU is required")
        String sku,

        @NotBlank(message = "Product name is required")
        String name,

        String description,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        String currency,

        @Min(value = 0, message = "Available quantity cannot be negative")
        int availableQuantity,

        List<String> tags
) {
}
