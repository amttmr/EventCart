package com.eventcart.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Configuration properties for external notification delivery providers.
 *
 * @param email SMTP email settings
 * @param sms Twilio SMS settings
 * @param contacts customer contact lookup used by local demos
 */
@ConfigurationProperties(prefix = "eventcart.notifications")
public record NotificationDeliveryProperties(
        Email email,
        Sms sms,
        Map<String, CustomerContact> contacts
) {
    /**
     * Applies safe defaults when optional notification configuration is absent.
     *
     * @param email SMTP email settings
     * @param sms Twilio SMS settings
     * @param contacts customer contact lookup
     */
    public NotificationDeliveryProperties {
        if (email == null) {
            email = new Email(false, "noreply@eventcart.local");
        }
        if (sms == null) {
            sms = new Sms(false, "", "", "", "https://api.twilio.com");
        }
        if (contacts == null) {
            contacts = Map.of();
        }
    }

    /**
     * SMTP email settings.
     *
     * @param enabled whether email delivery is enabled
     * @param from default sender address
     */
    public record Email(boolean enabled, String from) {
    }

    /**
     * Twilio SMS settings.
     *
     * @param enabled whether SMS delivery is enabled
     * @param accountSid Twilio account SID
     * @param authToken Twilio auth token
     * @param fromNumber Twilio sender number
     * @param baseUrl Twilio API base URL
     */
    public record Sms(
            boolean enabled,
            String accountSid,
            String authToken,
            String fromNumber,
            String baseUrl
    ) {
    }

    /**
     * Customer contact details for notification delivery.
     *
     * @param email customer email address
     * @param phoneNumber customer phone number in E.164 format
     */
    public record CustomerContact(String email, String phoneNumber) {
    }
}
