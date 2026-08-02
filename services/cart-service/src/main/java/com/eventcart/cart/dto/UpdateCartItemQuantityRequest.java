package com.eventcart.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

/**
 * Request body used to update the quantity of one cart item.
 *
 * @param quantity new item quantity
 */
public record UpdateCartItemQuantityRequest(
        @Schema(description = "Replacement quantity for the cart item", example = "3")
        @Min(value = 1, message = "Quantity must be at least one")
        int quantity
) {
}
