package com.eventcart.common.events;

import java.math.BigDecimal;

public record ProductCreatedEvent(
        EventMetadata metadata,
        String productId,
        String sku,
        String name,
        String category,
        BigDecimal price,
        String currency
) {
    public static final String EVENT_TYPE = "catalog.product.created";
    public static final int VERSION = 1;
}

