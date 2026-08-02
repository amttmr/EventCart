package com.eventcart.order.domain;

import java.math.BigDecimal;

/**
 * Immutable product snapshot embedded inside an order document.
 *
 * @param productId product ID from catalog-service
 * @param sku product SKU snapshot
 * @param productName product display name snapshot
 * @param unitPrice unit price captured when the order was placed
 * @param currency currency code
 * @param quantity ordered quantity
 * @param lineTotal unit price multiplied by quantity
 */
public record OrderItemDocument(
        String productId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        String currency,
        int quantity,
        BigDecimal lineTotal
) {
}
