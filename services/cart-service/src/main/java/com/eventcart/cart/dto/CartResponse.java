package com.eventcart.cart.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Public cart representation returned by cart APIs.
 *
 * @param cartId MongoDB-generated cart ID
 * @param customerId customer that owns the cart
 * @param items cart items
 * @param totalItems total quantity across all cart items
 * @param subtotal sum of all item line totals
 * @param currency currency used by the cart totals
 * @param version optimistic locking version maintained by MongoDB/Spring Data
 * @param updatedAt time when the cart was last modified
 */
public record CartResponse(
        String cartId,
        String customerId,
        List<CartItemResponse> items,
        int totalItems,
        BigDecimal subtotal,
        String currency,
        Long version,
        Instant updatedAt
) {
}

