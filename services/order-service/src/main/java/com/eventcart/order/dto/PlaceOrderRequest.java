package com.eventcart.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body used to place an order from a customer's current cart.
 *
 * @param customerId customer whose cart should be converted into an order
 */
public record PlaceOrderRequest(
        @Schema(description = "Customer whose current cart should be converted into an order", example = "customer-1")
        @NotBlank(message = "Customer ID is required")
        String customerId
) {
}
