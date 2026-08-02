package com.eventcart.catalog.dto;

import java.math.BigDecimal;

public record ProductSearchCriteria(
        String keyword,
        String category,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}

