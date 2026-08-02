package com.eventcart.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for catalog-service.
 *
 * <p>Springdoc can infer a basic specification automatically, but this
 * configuration adds service-specific metadata that makes Swagger UI easier to
 * understand and more useful for interview demos.</p>
 */
@Configuration
public class OpenApiConfig {
    /**
     * Builds the OpenAPI model exposed at {@code /v3/api-docs}.
     *
     * @return OpenAPI metadata for catalog-service
     */
    @Bean
    public OpenAPI catalogOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventCart Catalog Service API")
                        .version("0.1.0")
                        .description("Product catalog APIs for EventCart.")
                        .contact(new Contact().name("EventCart Learning Project"))
                        .license(new License().name("Learning Project")))
                .servers(List.of(new Server()
                        .url("http://localhost:8081")
                        .description("Local catalog-service")));
    }
}
