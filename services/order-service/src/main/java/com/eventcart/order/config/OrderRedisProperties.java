package com.eventcart.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Redis settings used by order-service.
 *
 * @param host Redis host
 * @param port Redis port
 * @param orderIdempotencyTtl time to keep order idempotency keys
 */
@ConfigurationProperties(prefix = "eventcart.redis")
public record OrderRedisProperties(
        String host,
        int port,
        Duration orderIdempotencyTtl
) {
    /**
     * Creates Redis properties with safe local defaults.
     *
     * @param host Redis host
     * @param port Redis port
     * @param orderIdempotencyTtl idempotency key TTL
     */
    public OrderRedisProperties {
        if (host == null || host.isBlank()) {
            host = "localhost";
        }
        if (port == 0) {
            port = 6379;
        }
        if (orderIdempotencyTtl == null) {
            orderIdempotencyTtl = Duration.ofMinutes(30);
        }
    }
}
