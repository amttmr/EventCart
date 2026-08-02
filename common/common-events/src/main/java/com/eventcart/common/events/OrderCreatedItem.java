package com.eventcart.common.events;

import java.math.BigDecimal;

/**
 * Product snapshot included in an {@link OrderCreatedEvent}.
 *
 * <p>The order event carries item details so consumers such as inventory,
 * payment, notification, and analytics can process the order without making
 * synchronous calls back to order-service or cart-service.</p>
 *
 * @param productId product ID from catalog-service
 * @param sku product SKU snapshot
 * @param productName product display name snapshot
 * @param unitPrice unit price captured when the order was placed
 * @param currency currency code for the item price
 * @param quantity quantity ordered
 * @param lineTotal unit price multiplied by quantity
 */
public record OrderCreatedItem(
        String productId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        String currency,
        int quantity,
        BigDecimal lineTotal
) {
}
