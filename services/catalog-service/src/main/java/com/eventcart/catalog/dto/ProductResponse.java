package com.eventcart.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(
        String id,
        String sku,
        String name,
        String description,
        String category,
        BigDecimal price,
        String currency,
        int availableQuantity,
        List<String> tags,
        boolean active,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}

