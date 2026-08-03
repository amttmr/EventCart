package com.eventcart.payment;

import com.eventcart.payment.config.PaymentSimulationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstrap class for the EventCart payment service.
 */
@SpringBootApplication(scanBasePackages = "com.eventcart")
@EnableKafka
@EnableScheduling
@EnableConfigurationProperties(PaymentSimulationProperties.class)
public class PaymentServiceApplication {
    /**
     * Starts the payment service Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
