package com.eventcart.catalog.repository;

import com.eventcart.catalog.domain.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for product documents.
 *
 * <p>The repository provides standard CRUD operations and derived query methods
 * for catalog-specific lookup rules.</p>
 */
public interface ProductRepository extends MongoRepository<ProductDocument, String> {
    /**
     * Checks whether a product already exists with the given SKU.
     *
     * @param sku business-facing stock keeping unit
     * @return {@code true} when a product with the SKU already exists
     */
    boolean existsBySku(String sku);
}
