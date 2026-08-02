package com.eventcart.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Public product representation returned by catalog APIs.
 *
 * @param id MongoDB-generated product ID
 * @param sku business-facing stock keeping unit
 * @param name product display name
 * @param description product description
 * @param category product category
 * @param price product price
 * @param currency currency code for the price
 * @param availableQuantity current available quantity
 * @param tags searchable product tags
 * @param active whether the product is visible for normal shopping flows
 * @param version optimistic locking version maintained by MongoDB/Spring Data
 * @param createdAt time when the product was created
 * @param updatedAt time when the product was last modified
 */
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
