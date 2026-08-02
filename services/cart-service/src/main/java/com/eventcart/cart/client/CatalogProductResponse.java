package com.eventcart.cart.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Product response contract consumed from catalog-service.
 *
 * <p>This record intentionally duplicates the fields cart-service needs from
 * the catalog API instead of importing catalog-service classes. That keeps the
 * service boundary HTTP-based and independent.</p>
 *
 * @param id product ID from catalog-service
 * @param sku product SKU
 * @param name product display name
 * @param description product description
 * @param category product category
 * @param price product price
 * @param currency currency code
 * @param availableQuantity currently available quantity reported by catalog-service
 * @param tags product tags
 * @param active whether the product is active
 * @param version product optimistic locking version
 * @param createdAt product creation timestamp
 * @param updatedAt product update timestamp
 */
public record CatalogProductResponse(
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

