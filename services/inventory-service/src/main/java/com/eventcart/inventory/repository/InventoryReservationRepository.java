package com.eventcart.inventory.repository;

import com.eventcart.inventory.domain.InventoryReservationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for inventory reservation documents.
 */
public interface InventoryReservationRepository extends MongoRepository<InventoryReservationDocument, String> {
    /**
     * Finds the reservation result for an order.
     *
     * @param orderId order ID
     * @return optional reservation document
     */
    Optional<InventoryReservationDocument> findByOrderId(String orderId);
}
