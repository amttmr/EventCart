package com.eventcart.cart.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * Configuration properties for HTTP calls from cart-service to catalog-service.
 *
 * @param baseUrl base URL of catalog-service
 * @param connectTimeout maximum time allowed to establish the HTTP connection
 * @param readTimeout maximum time allowed to wait for the HTTP response
 */
@ConfigurationProperties(prefix = "eventcart.clients.catalog")
public record CatalogClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
    /**
     * Creates catalog client properties with safe local defaults.
     *
     * @param baseUrl base URL of catalog-service
     * @param connectTimeout connection timeout
     * @param readTimeout response timeout
     */
    public CatalogClientProperties {
        if (baseUrl == null) {
            baseUrl = URI.create("http://localhost:8081");
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(2);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(3);
        }
    }
}

