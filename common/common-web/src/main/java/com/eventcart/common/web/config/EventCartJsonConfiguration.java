package com.eventcart.common.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared JSON configuration for EventCart services.
 *
 * <p>Spring Boot usually creates an {@link ObjectMapper} automatically, but
 * this fallback keeps shared components such as the transactional outbox
 * reliable in every local run configuration.</p>
 */
@Configuration
public class EventCartJsonConfiguration {
    /**
     * Creates the shared Jackson mapper when the application does not already
     * provide one.
     *
     * @return object mapper with Java time support enabled
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper eventCartObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
