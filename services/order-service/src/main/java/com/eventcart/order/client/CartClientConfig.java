package com.eventcart.order.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Spring configuration for cart-service HTTP client beans.
 */
@Configuration
@EnableConfigurationProperties(CartClientProperties.class)
public class CartClientConfig {
    /**
     * Creates the REST client used by order-service to call cart-service.
     *
     * @param properties cart client timeout and URL settings
     * @return configured RestClient instance
     */
    @Bean
    public RestClient cartRestClient(CartClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }
}
