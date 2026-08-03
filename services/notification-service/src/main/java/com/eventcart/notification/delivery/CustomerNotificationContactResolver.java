package com.eventcart.notification.delivery;

import com.eventcart.notification.config.NotificationDeliveryProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves customer contact details for notification providers.
 */
@Component
public class CustomerNotificationContactResolver {
    private final NotificationDeliveryProperties properties;

    /**
     * Creates the contact resolver.
     *
     * @param properties notification delivery properties
     */
    public CustomerNotificationContactResolver(NotificationDeliveryProperties properties) {
        this.properties = properties;
    }

    /**
     * Resolves the customer's email address.
     *
     * @param customerId customer ID
     * @return configured email address when available
     */
    public Optional<String> emailForCustomer(String customerId) {
        return contact(customerId).map(NotificationDeliveryProperties.CustomerContact::email)
                .filter(value -> !value.isBlank());
    }

    /**
     * Resolves the customer's SMS phone number.
     *
     * @param customerId customer ID
     * @return configured phone number when available
     */
    public Optional<String> phoneForCustomer(String customerId) {
        return contact(customerId).map(NotificationDeliveryProperties.CustomerContact::phoneNumber)
                .filter(value -> !value.isBlank());
    }

    /**
     * Resolves the configured contact record for a customer.
     *
     * @param customerId customer ID
     * @return configured contact when available
     */
    private Optional<NotificationDeliveryProperties.CustomerContact> contact(String customerId) {
        return Optional.ofNullable(properties.contacts().get(customerId));
    }
}
