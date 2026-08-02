package com.eventcart.inventory.repository;

import com.eventcart.inventory.domain.InventoryItemDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for inventory stock documents.
 */
public interface InventoryItemRepository extends MongoRepository<InventoryItemDocument, String> {
}
