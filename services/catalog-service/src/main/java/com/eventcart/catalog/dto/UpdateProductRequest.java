package com.eventcart.catalog.dto;

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

        List<String> tags,

        boolean active
) {
}
