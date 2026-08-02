package com.eventcart.order.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Cart response contract consumed from cart-service.
 *
 * @param cartId MongoDB-generated cart ID
 * @param customerId customer that owns the cart
 * @param items cart item snapshots
 * @param totalItems total quantity across all cart items
 * @param subtotal sum of all item line totals
 * @param currency currency used by the cart totals
 * @param version optimistic locking version maintained by cart-service
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
