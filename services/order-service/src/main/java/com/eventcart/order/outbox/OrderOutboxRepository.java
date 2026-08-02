package com.eventcart.order.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoDB repository for order-service outbox events.
 */
public interface OrderOutboxRepository extends MongoRepository<OrderOutboxEventDocument, String> {
    /**
     * Finds pending events in creation order so publishing is predictable.
     *
     * @param status outbox status to fetch
     * @return matching outbox events
     */
    List<OrderOutboxEventDocument> findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
