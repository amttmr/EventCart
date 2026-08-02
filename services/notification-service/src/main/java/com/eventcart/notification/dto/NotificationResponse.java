package com.eventcart.notification.dto;

import com.eventcart.notification.domain.NotificationChannel;
import com.eventcart.notification.domain.NotificationStatus;
import com.eventcart.notification.domain.NotificationType;

import java.time.Instant;

/**
 * Public API response for one notification.
 *
 * @param notificationId notification ID
 * @param customerId customer ID
 * @param orderId related order ID
 * @param type notification type
 * @param channel delivery channel
 * @param status read status
 * @param title notification title
 * @param message notification message
 * @param correlationId trace correlation ID
 * @param createdAt creation timestamp
 * @param readAt read timestamp
 */
public record NotificationResponse(
        String notificationId,
        String customerId,
        String orderId,
        NotificationType type,
        NotificationChannel channel,
        NotificationStatus status,
        String title,
        String message,
        String correlationId,
        Instant createdAt,
        Instant readAt
) {
}
