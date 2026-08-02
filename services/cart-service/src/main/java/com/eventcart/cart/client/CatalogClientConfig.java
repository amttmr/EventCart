package com.eventcart.cart.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Spring configuration for catalog-service HTTP client beans.
 */
@Configuration
@EnableConfigurationProperties(CatalogClientProperties.class)
public class CatalogClientConfig {
    /**
     * Creates the REST client used by cart-service to call catalog-service.
     *
     * @param properties catalog client timeout and URL settings
     * @return configured RestClient instance
     */
    @Bean
    public RestClient catalogRestClient(CatalogClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }
}

