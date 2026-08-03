package com.eventcart.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstrap class for the EventCart inventory service.
 */
@SpringBootApplication(scanBasePackages = "com.eventcart")
@EnableKafka
@EnableScheduling
public class InventoryServiceApplication {
    /**
     * Starts the inventory service Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
