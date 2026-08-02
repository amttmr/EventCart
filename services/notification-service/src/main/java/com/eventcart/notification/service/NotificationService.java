package com.eventcart.notification.service;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.notification.domain.NotificationDocument;
import com.eventcart.notification.domain.NotificationStatus;
import com.eventcart.notification.dto.NotificationResponse;
import com.eventcart.notification.exception.NotificationNotFoundException;
import com.eventcart.notification.mapper.NotificationMapper;
import com.eventcart.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Application service that records and reads customer notifications.
 */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Creates a notification service.
     *
     * @param notificationRepository repository for notification persistence
     * @param notificationMapper mapper for events and API DTOs
     */
    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    /**
     * Records an order-created notification.
     *
     * @param event order-created event
     * @return stored notification response
     */
    public NotificationResponse recordOrderCreated(OrderCreatedEvent event) {
        return saveIfNew(event.metadata().eventId(), notificationMapper.fromOrderCreated(event));
    }

    /**
     * Records an inventory-failed notification.
     *
     * @param event inventory-failed event
     * @return stored notification response
     */
    public NotificationResponse recordInventoryFailed(InventoryReservationFailedEvent event) {
        return saveIfNew(event.metadata().eventId(), notificationMapper.fromInventoryFailed(event));
    }

    /**
     * Records a payment-completed notification.
     *
     * @param event payment-completed event
     * @return stored notification response
     */
    public NotificationResponse recordPaymentCompleted(PaymentCompletedEvent event) {
        return saveIfNew(event.metadata().eventId(), notificationMapper.fromPaymentCompleted(event));
    }

    /**
     * Records a payment-failed notification.
     *
     * @param event payment-failed event
     * @return stored notification response
     */
    public NotificationResponse recordPaymentFailed(PaymentFailedEvent event) {
        return saveIfNew(event.metadata().eventId(), notificationMapper.fromPaymentFailed(event));
    }

    /**
     * Returns customer notifications newest first.
     *
     * @param customerId customer ID
     * @return customer notifications
     */
    public List<NotificationResponse> getNotificationsForCustomer(String customerId) {
        log.debug("Fetching notifications customerId={}", customerId);
        return notificationMapper.toResponses(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    /**
     * Returns one notification by ID.
     *
     * @param notificationId notification ID
     * @return notification response
     */
    public NotificationResponse getNotification(String notificationId) {
        return notificationMapper.toResponse(findNotification(notificationId));
    }

    /**
     * Marks one notification as read.
     *
     * @param notificationId notification ID
     * @return updated notification response
     */
    public NotificationResponse markRead(String notificationId) {
        NotificationDocument notification = findNotification(notificationId);
        if (notification.getStatus() == NotificationStatus.READ) {
            return notificationMapper.toResponse(notification);
        }
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(Instant.now());
        NotificationDocument saved = notificationRepository.save(notification);
        log.info("Notification marked read notificationId={} customerId={}", saved.getId(), saved.getCustomerId());
        return notificationMapper.toResponse(saved);
    }

    /**
     * Stores a notification only when the source event has not been processed.
     *
     * @param eventId Kafka event ID
     * @param notification notification to save
     * @return saved or existing notification response
     */
    private NotificationResponse saveIfNew(String eventId, NotificationDocument notification) {
        return notificationRepository.findBySourceEventId(eventId)
                .map(existing -> {
                    log.info("Skipping duplicate notification eventId={} notificationId={}", eventId, existing.getId());
                    return notificationMapper.toResponse(existing);
                })
                .orElseGet(() -> {
                    NotificationDocument saved = notificationRepository.save(notification);
                    log.info("Notification stored notificationId={} customerId={} type={} eventId={}",
                            saved.getId(), saved.getCustomerId(), saved.getType(), eventId);
                    return notificationMapper.toResponse(saved);
                });
    }

    /**
     * Loads a notification or throws a not-found exception.
     *
     * @param notificationId notification ID
     * @return persisted notification
     */
    private NotificationDocument findNotification(String notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationId));
    }
}
