package com.eventcart.order.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Explicit Redis infrastructure for order-service idempotency.
 */
@Configuration
@EnableConfigurationProperties(OrderRedisProperties.class)
public class RedisConfig {
    /**
     * Creates the Redis connection factory used by order-service.
     *
     * @param properties Redis connection properties
     * @return Redis connection factory
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(OrderRedisProperties properties) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(properties.host(), properties.port());
        return new LettuceConnectionFactory(configuration);
    }

    /**
     * Creates the StringRedisTemplate used for idempotency keys.
     *
     * @param redisConnectionFactory Redis connection factory
     * @return string Redis template
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
}
