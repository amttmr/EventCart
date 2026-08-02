package com.eventcart.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body used to place an order from a customer's current cart.
 *
 * @param customerId customer whose cart should be converted into an order
 */
public record PlaceOrderRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId
) {
}
