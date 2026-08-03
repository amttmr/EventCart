package com.eventcart.inventory.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoDB repository for inventory-service outbox events.
 */
public interface InventoryOutboxRepository extends MongoRepository<InventoryOutboxEventDocument, String> {
    /**
     * Finds pending events in creation order so publication is deterministic.
     *
     * @param status outbox status to fetch
     * @return matching outbox events
     */
    List<InventoryOutboxEventDocument> findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
