package com.eventcart.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for notification-service.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Creates OpenAPI metadata shown in Swagger UI.
     *
     * @return OpenAPI metadata
     */
    @Bean
    public OpenAPI notificationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventCart Notification Service API")
                        .version("0.1.0")
                        .description("APIs for customer notification lookup and read state."));
    }
}
