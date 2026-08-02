package com.eventcart.catalog.repository;

import com.eventcart.catalog.domain.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<ProductDocument, String> {
    boolean existsBySku(String sku);
}

