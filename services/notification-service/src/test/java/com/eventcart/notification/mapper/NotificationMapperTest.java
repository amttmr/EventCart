package com.eventcart.notification.mapper;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.notification.domain.NotificationStatus;
import com.eventcart.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NotificationMapper}.
 */
class NotificationMapperTest {
    private final NotificationMapper notificationMapper = new NotificationMapper();

    /**
     * Verifies that a payment-completed event creates a customer notification.
     */
    @Test
    void fromPaymentCompletedShouldCreateNotificationDocument() {
        var notification = notificationMapper.fromPaymentCompleted(new PaymentCompletedEvent(
                EventMetadata.create(PaymentCompletedEvent.EVENT_TYPE, PaymentCompletedEvent.VERSION, "correlation-1"),
                "payment-1",
                "order-1",
                "customer-1",
                new BigDecimal("6999.00"),
                "INR",
                "MockPay-transaction-1"
        ));

        assertThat(notification.getCustomerId()).isEqualTo("customer-1");
        assertThat(notification.getOrderId()).isEqualTo("order-1");
        assertThat(notification.getType()).isEqualTo(NotificationType.PAYMENT_COMPLETED);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(notification.getTitle()).isEqualTo("Payment completed");
    }
}
