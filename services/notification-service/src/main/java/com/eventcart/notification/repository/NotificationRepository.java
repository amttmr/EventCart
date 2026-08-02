package com.eventcart.notification.repository;

import com.eventcart.notification.domain.NotificationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for notification records.
 */
public interface NotificationRepository extends MongoRepository<NotificationDocument, String> {
    /**
     * Finds notifications for a customer ordered newest first.
     *
     * @param customerId customer ID
     * @return customer notifications
     */
    List<NotificationDocument> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    /**
     * Finds a notification by its source Kafka event ID.
     *
     * @param sourceEventId event ID from Kafka metadata
     * @return matching notification if one exists
     */
    Optional<NotificationDocument> findBySourceEventId(String sourceEventId);
}
