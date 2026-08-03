package com.eventcart.notification.delivery;

import com.eventcart.notification.domain.NotificationDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Coordinates delivery of persisted notifications to external providers.
 */
@Service
public class NotificationDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final List<NotificationDeliveryProvider> providers;

    /**
     * Creates the notification delivery service.
     *
     * @param providers configured delivery providers
     */
    public NotificationDeliveryService(List<NotificationDeliveryProvider> providers) {
        this.providers = providers;
    }

    /**
     * Delivers a saved notification through every configured provider.
     *
     * @param notification saved notification document
     */
    public void deliver(NotificationDocument notification) {
        for (NotificationDeliveryProvider provider : providers) {
            try {
                provider.deliver(notification);
            } catch (RuntimeException ex) {
                log.warn("Notification provider failed provider={} notificationId={} customerId={}",
                        provider.providerName(), notification.getId(), notification.getCustomerId(), ex);
            }
        }
    }
}
