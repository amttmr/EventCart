package com.eventcart.order.dto;

import java.math.BigDecimal;

/**
 * Public representation of one order item.
 *
 * @param productId product ID from catalog-service
 * @param sku product SKU snapshot
 * @param productName product name snapshot
 * @param unitPrice unit price snapshot
 * @param currency currency code
 * @param quantity ordered quantity
 * @param lineTotal unit price multiplied by quantity
 */
public record OrderItemResponse(
        String productId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        String currency,
        int quantity,
        BigDecimal lineTotal
) {
}
