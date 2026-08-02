package com.eventcart.order.client;

import java.math.BigDecimal;

/**
 * Cart item response contract consumed from cart-service.
 *
 * @param productId product ID from catalog-service
 * @param sku product SKU snapshot
 * @param productName product name snapshot
 * @param unitPrice unit price snapshot
 * @param currency currency code
 * @param quantity quantity in the cart
 * @param lineTotal unit price multiplied by quantity
 */
public record CartItemResponse(
        String productId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        String currency,
        int quantity,
        BigDecimal lineTotal
) {
}
