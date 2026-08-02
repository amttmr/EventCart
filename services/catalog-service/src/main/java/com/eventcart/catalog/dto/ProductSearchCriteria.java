package com.eventcart.catalog.dto;

import java.math.BigDecimal;

/**
 * Internal search criteria object used by the catalog service layer.
 *
 * @param keyword optional keyword matched against name, description, and tags
 * @param category optional category filter
 * @param active optional active/inactive filter
 * @param minPrice optional minimum price filter
 * @param maxPrice optional maximum price filter
 */
public record ProductSearchCriteria(
        String keyword,
        String category,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
