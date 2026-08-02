package com.eventcart.notification.domain;

/**
 * Business reason for a notification.
 */
public enum NotificationType {
    ORDER_CREATED,
    INVENTORY_FAILED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED
}
