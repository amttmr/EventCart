package com.eventcart.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap class for the EventCart catalog service.
 *
 * <p>The explicit scan base package allows this service to discover shared
 * components from {@code com.eventcart.common} in addition to catalog-specific
 * beans.</p>
 */
@SpringBootApplication(scanBasePackages = "com.eventcart")
public class CatalogServiceApplication {
    /**
     * Starts the catalog service Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
