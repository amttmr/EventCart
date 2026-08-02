package com.eventcart.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cloud Gateway route definitions for EventCart services.
 */
@Configuration
public class GatewayRoutesConfig {
    /**
     * Creates gateway routes from public paths to backend services.
     *
     * @param builder route locator builder
     * @param catalogServiceUrl catalog-service base URL
     * @param cartServiceUrl cart-service base URL
     * @param orderServiceUrl order-service base URL
     * @param inventoryServiceUrl inventory-service base URL
     * @param paymentServiceUrl payment-service base URL
     * @param notificationServiceUrl notification-service base URL
     * @return configured route locator
     */
    @Bean
    public RouteLocator eventCartRoutes(
            RouteLocatorBuilder builder,
            @Value("${eventcart.gateway.services.catalog-service-url}") String catalogServiceUrl,
            @Value("${eventcart.gateway.services.cart-service-url}") String cartServiceUrl,
            @Value("${eventcart.gateway.services.order-service-url}") String orderServiceUrl,
            @Value("${eventcart.gateway.services.inventory-service-url}") String inventoryServiceUrl,
            @Value("${eventcart.gateway.services.payment-service-url}") String paymentServiceUrl,
            @Value("${eventcart.gateway.services.notification-service-url}") String notificationServiceUrl
    ) {
        return builder.routes()
                .route("catalog-service", route -> route.path("/api/v1/products/**").uri(catalogServiceUrl))
                .route("cart-service", route -> route.path("/api/v1/carts/**").uri(cartServiceUrl))
                .route("order-service", route -> route.path("/api/v1/orders/**").uri(orderServiceUrl))
                .route("inventory-service", route -> route.path("/api/v1/inventory/**").uri(inventoryServiceUrl))
                .route("payment-service", route -> route.path("/api/v1/payments/**").uri(paymentServiceUrl))
                .route("notification-service", route -> route.path("/api/v1/notifications/**").uri(notificationServiceUrl))
                .build();
    }
}
