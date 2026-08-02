package com.eventcart.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for cart-service.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Builds the OpenAPI model exposed at {@code /v3/api-docs}.
     *
     * @return OpenAPI metadata for cart-service
     */
    @Bean
    public OpenAPI cartOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventCart Cart Service API")
                        .version("0.1.0")
                        .description("Shopping cart APIs for EventCart.")
                        .contact(new Contact().name("EventCart Learning Project"))
                        .license(new License().name("Learning Project")))
                .servers(List.of(new Server()
                        .url("http://localhost:8082")
                        .description("Local cart-service")));
    }
}

