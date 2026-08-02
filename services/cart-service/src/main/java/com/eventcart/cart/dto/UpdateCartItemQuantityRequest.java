package com.eventcart.cart.dto;

import jakarta.validation.constraints.Min;

/**
 * Request body used to update the quantity of one cart item.
 *
 * @param quantity new item quantity
 */
public record UpdateCartItemQuantityRequest(
        @Min(value = 1, message = "Quantity must be at least one")
        int quantity
) {
}

