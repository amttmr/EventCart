package com.eventcart.notification.delivery;

import com.eventcart.notification.config.NotificationDeliveryProperties;
import com.eventcart.notification.domain.NotificationDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * SMTP-backed notification delivery provider.
 */
@Component
public class EmailNotificationDeliveryProvider implements NotificationDeliveryProvider {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationDeliveryProvider.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final NotificationDeliveryProperties properties;
    private final CustomerNotificationContactResolver contactResolver;

    /**
     * Creates the email delivery provider.
     *
     * @param mailSenderProvider Spring mail sender provider
     * @param properties delivery configuration
     * @param contactResolver customer contact resolver
     */
    public EmailNotificationDeliveryProvider(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            NotificationDeliveryProperties properties,
            CustomerNotificationContactResolver contactResolver
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.properties = properties;
        this.contactResolver = contactResolver;
    }

    /**
     * Returns this provider's name.
     *
     * @return provider name
     */
    @Override
    public String providerName() {
        return "smtp-email";
    }

    /**
     * Sends the notification through SMTP when email delivery is enabled.
     *
     * @param notification saved notification document
     */
    @Override
    public void deliver(NotificationDocument notification) {
        if (!properties.email().enabled()) {
            log.debug("Email delivery disabled notificationId={}", notification.getId());
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Email delivery skipped because JavaMailSender is not configured notificationId={}", notification.getId());
            return;
        }

        contactResolver.emailForCustomer(notification.getCustomerId()).ifPresentOrElse(email -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.email().from());
            message.setTo(email);
            message.setSubject(notification.getTitle());
            message.setText(notification.getMessage());
            mailSender.send(message);
            log.info("Email notification sent notificationId={} customerId={} to={}",
                    notification.getId(), notification.getCustomerId(), email);
        }, () -> log.warn("Email delivery skipped because customer email is not configured customerId={} notificationId={}",
                notification.getCustomerId(), notification.getId()));
    }
}
