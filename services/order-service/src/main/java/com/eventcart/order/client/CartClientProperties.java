package com.eventcart.order.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * Configuration properties for HTTP calls from order-service to cart-service.
 *
 * @param baseUrl base URL of cart-service
 * @param connectTimeout maximum time allowed to establish the HTTP connection
 * @param readTimeout maximum time allowed to wait for the HTTP response
 */
@ConfigurationProperties(prefix = "eventcart.clients.cart")
public record CartClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
    /**
     * Creates cart client properties with safe local defaults.
     *
     * @param baseUrl base URL of cart-service
     * @param connectTimeout connection timeout
     * @param readTimeout response timeout
     */
    public CartClientProperties {
        if (baseUrl == null) {
            baseUrl = URI.create("http://localhost:8082");
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(2);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(3);
        }
    }
}
