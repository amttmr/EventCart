package com.eventcart.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Bootstrap class for the EventCart notification service.
 */
@SpringBootApplication(scanBasePackages = "com.eventcart")
@EnableKafka
@ConfigurationPropertiesScan(basePackages = "com.eventcart.notification.config")
public class NotificationServiceApplication {
    /**
     * Starts the notification service Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
