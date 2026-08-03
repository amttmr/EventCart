package com.eventcart.notification.delivery;

import com.eventcart.notification.config.NotificationDeliveryProperties;
import com.eventcart.notification.domain.NotificationDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Twilio REST API backed SMS notification delivery provider.
 */
@Component
public class SmsNotificationDeliveryProvider implements NotificationDeliveryProvider {
    private static final Logger log = LoggerFactory.getLogger(SmsNotificationDeliveryProvider.class);

    private final NotificationDeliveryProperties properties;
    private final CustomerNotificationContactResolver contactResolver;
    private final RestClient restClient;

    /**
     * Creates the SMS delivery provider.
     *
     * @param properties delivery configuration
     * @param contactResolver customer contact resolver
     */
    public SmsNotificationDeliveryProvider(
            NotificationDeliveryProperties properties,
            CustomerNotificationContactResolver contactResolver
    ) {
        this.properties = properties;
        this.contactResolver = contactResolver;
        this.restClient = RestClient.builder()
                .baseUrl(properties.sms().baseUrl())
                .defaultHeaders(headers -> headers.setBasicAuth(
                        properties.sms().accountSid(),
                        properties.sms().authToken()
                ))
                .build();
    }

    /**
     * Returns this provider's name.
     *
     * @return provider name
     */
    @Override
    public String providerName() {
        return "twilio-sms";
    }

    /**
     * Sends the notification through Twilio when SMS delivery is enabled.
     *
     * @param notification saved notification document
     */
    @Override
    public void deliver(NotificationDocument notification) {
        if (!properties.sms().enabled()) {
            log.debug("SMS delivery disabled notificationId={}", notification.getId());
            return;
        }
        contactResolver.phoneForCustomer(notification.getCustomerId()).ifPresentOrElse(
                phoneNumber -> sendSms(notification, phoneNumber),
                () -> log.warn("SMS delivery skipped because customer phone is not configured customerId={} notificationId={}",
                        notification.getCustomerId(), notification.getId())
        );
    }

    /**
     * Calls Twilio's message API for one notification.
     *
     * @param notification saved notification document
     * @param phoneNumber destination phone number
     */
    private void sendSms(NotificationDocument notification, String phoneNumber) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("From", properties.sms().fromNumber());
        form.add("To", phoneNumber);
        form.add("Body", notification.getTitle() + " - " + notification.getMessage());

        restClient.post()
                .uri("/2010-04-01/Accounts/{accountSid}/Messages.json", properties.sms().accountSid())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();

        log.info("SMS notification sent notificationId={} customerId={} to={}",
                notification.getId(), notification.getCustomerId(), phoneNumber);
    }
}
