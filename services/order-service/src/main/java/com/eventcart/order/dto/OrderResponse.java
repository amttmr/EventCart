package com.eventcart.order.dto;

import com.eventcart.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Public representation of an order.
 *
 * @param orderId MongoDB-generated order ID
 * @param customerId customer that placed the order
 * @param items order item snapshots
 * @param totalAmount order total amount
 * @param currency currency code
 * @param status current order status
 * @param version optimistic locking version maintained by MongoDB/Spring Data
 * @param createdAt time when the order was created
 * @param updatedAt time when the order was last modified
 */
public record OrderResponse(
        String orderId,
        String customerId,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        String currency,
        OrderStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
