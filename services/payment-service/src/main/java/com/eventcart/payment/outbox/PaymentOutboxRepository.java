package com.eventcart.payment.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoDB repository for payment-service outbox events.
 */
public interface PaymentOutboxRepository extends MongoRepository<PaymentOutboxEventDocument, String> {
    /**
     * Finds pending events in creation order so publication is deterministic.
     *
     * @param status outbox status to fetch
     * @return matching outbox events
     */
    List<PaymentOutboxEventDocument> findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
