package com.eventcart.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap class for the EventCart cart service.
 *
 * <p>The scan base package includes shared EventCart modules such as
 * {@code common-web} in addition to cart-service classes.</p>
 */
@SpringBootApplication(scanBasePackages = "com.eventcart")
public class CartServiceApplication {
    /**
     * Starts the cart service Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}

