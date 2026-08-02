package com.eventcart.cart.repository;

import com.eventcart.cart.domain.CartDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for customer cart documents.
 */
public interface CartRepository extends MongoRepository<CartDocument, String> {
    /**
     * Finds the active cart for a customer.
     *
     * @param customerId customer ID
     * @return optional cart document
     */
    Optional<CartDocument> findByCustomerId(String customerId);
}

