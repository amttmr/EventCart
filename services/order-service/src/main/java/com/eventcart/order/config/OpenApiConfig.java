package com.eventcart.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for order-service.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Builds the OpenAPI model exposed at {@code /v3/api-docs}.
     *
     * @return OpenAPI metadata for order-service
     */
    @Bean
    public OpenAPI orderOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventCart Order Service API")
                        .version("0.1.0")
                        .description("Order placement APIs for EventCart.")
                        .contact(new Contact().name("EventCart Learning Project"))
                        .license(new License().name("Learning Project")))
                .servers(List.of(new Server()
                        .url("http://localhost:8083")
                        .description("Local order-service")));
    }
}
