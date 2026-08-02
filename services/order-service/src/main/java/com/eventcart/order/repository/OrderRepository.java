package com.eventcart.order.repository;

import com.eventcart.order.domain.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for order documents.
 */
public interface OrderRepository extends MongoRepository<OrderDocument, String> {
    /**
     * Finds orders for a customer ordered by newest first.
     *
     * @param customerId customer ID
     * @return customer orders
     */
    List<OrderDocument> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
