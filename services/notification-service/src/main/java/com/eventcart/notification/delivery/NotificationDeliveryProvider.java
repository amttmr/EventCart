package com.eventcart.notification.delivery;

import com.eventcart.notification.domain.NotificationDocument;

/**
 * Provider contract for external notification delivery.
 */
public interface NotificationDeliveryProvider {
    /**
     * Returns the provider name used in logs and interviews.
     *
     * @return provider name
     */
    String providerName();

    /**
     * Attempts to deliver one notification through the provider.
     *
     * @param notification saved notification document
     */
    void deliver(NotificationDocument notification);
}
