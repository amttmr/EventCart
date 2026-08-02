package com.eventcart.cart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body used to add a product snapshot to a customer's cart.
 *
 * @param productId product ID from catalog-service
 * @param sku product SKU copied from catalog-service
 * @param productName product display name copied from catalog-service
 * @param unitPrice product unit price at the time it is added to cart
 * @param currency currency code for the unit price
 * @param quantity quantity to add
 */
public record AddCartItemRequest(
        @NotBlank(message = "Product ID is required")
        String productId,

        @NotBlank(message = "SKU is required")
        String sku,

        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be greater than zero")
        BigDecimal unitPrice,

        @NotBlank(message = "Currency is required")
        String currency,

        @Min(value = 1, message = "Quantity must be at least one")
        int quantity
) {
}

