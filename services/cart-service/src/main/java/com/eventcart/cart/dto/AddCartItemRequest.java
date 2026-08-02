package com.eventcart.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body used to add a product to a customer's cart.
 *
 * <p>The caller sends only product ID and quantity. Cart-service fetches the
 * product details from catalog-service and stores a product snapshot in the
 * cart document.</p>
 *
 * @param productId product ID to fetch from catalog-service
 * @param quantity quantity to add
 */
public record AddCartItemRequest(
        @NotBlank(message = "Product ID is required")
        String productId,

        @Min(value = 1, message = "Quantity must be at least one")
        int quantity
) {
}
