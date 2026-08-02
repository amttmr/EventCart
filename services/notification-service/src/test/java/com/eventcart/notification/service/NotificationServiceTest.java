package com.eventcart.notification.service;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.notification.domain.NotificationDocument;
import com.eventcart.notification.domain.NotificationStatus;
import com.eventcart.notification.dto.NotificationResponse;
import com.eventcart.notification.mapper.NotificationMapper;
import com.eventcart.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NotificationService}.
 */
class NotificationServiceTest {
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationMapper notificationMapper = new NotificationMapper();
    private final NotificationService notificationService = new NotificationService(notificationRepository, notificationMapper);

    /**
     * Verifies that duplicate source events do not create duplicate notifications.
     */
    @Test
    void recordPaymentFailedShouldSkipDuplicateSourceEvent() {
        NotificationDocument existing = notificationMapper.fromPaymentFailed(paymentFailedEvent());
        existing.setId("notification-1");
        when(notificationRepository.findBySourceEventId("event-1")).thenReturn(Optional.of(existing));

        NotificationResponse response = notificationService.recordPaymentFailed(paymentFailedEvent());

        assertThat(response.notificationId()).isEqualTo("notification-1");
        verify(notificationRepository, never()).save(any());
    }

    /**
     * Verifies that marking a notification read updates status and timestamp.
     */
    @Test
    void markReadShouldSetReadState() {
        NotificationDocument existing = notificationMapper.fromPaymentFailed(paymentFailedEvent());
        existing.setId("notification-1");
        when(notificationRepository.findById("notification-1")).thenReturn(Optional.of(existing));
        when(notificationRepository.save(existing)).thenReturn(existing);

        NotificationResponse response = notificationService.markRead("notification-1");

        assertThat(response.status()).isEqualTo(NotificationStatus.READ);
        assertThat(response.readAt()).isNotNull();
        verify(notificationRepository).save(existing);
    }

    /**
     * Creates a payment-failed event for notification tests.
     *
     * @return payment-failed event
     */
    private PaymentFailedEvent paymentFailedEvent() {
        return new PaymentFailedEvent(
                new EventMetadata(
                        "event-1",
                        PaymentFailedEvent.EVENT_TYPE,
                        PaymentFailedEvent.VERSION,
                        "correlation-1",
                        java.time.Instant.now()
                ),
                "payment-1",
                "order-1",
                "customer-1",
                new BigDecimal("6999.00"),
                "INR",
                "Mock payment declined"
        );
    }
}
