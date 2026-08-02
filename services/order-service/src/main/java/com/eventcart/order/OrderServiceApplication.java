package com.eventcart.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap class for the EventCart order service.
 *
 * <p>The scan base package allows the service to discover shared EventCart
 * components from common modules in addition to order-specific beans.</p>
 */
@SpringBootApplication(scanBasePackages = "com.eventcart")
public class OrderServiceApplication {
    /**
     * Starts the order service Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
