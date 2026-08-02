package com.eventcart.order.service;

import com.eventcart.order.config.OrderRedisProperties;
import com.eventcart.order.exception.DuplicateOrderRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Redis-backed idempotency helper for order placement.
 */
@Service
public class OrderIdempotencyService {
    private static final Logger log = LoggerFactory.getLogger(OrderIdempotencyService.class);
    private static final String KEY_PREFIX = "eventcart:orders:idempotency:";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String ORDER_PREFIX = "ORDER:";

    private final StringRedisTemplate redisTemplate;
    private final OrderRedisProperties properties;

    /**
     * Creates an order idempotency service.
     *
     * @param redisTemplate Redis template for string keys
     * @param properties Redis idempotency settings
     */
    public OrderIdempotencyService(StringRedisTemplate redisTemplate, OrderRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * Starts processing for an idempotency key or returns the existing order ID.
     *
     * @param idempotencyKey client-provided idempotency key
     * @return existing order ID when this request was already completed
     */
    public Optional<String> begin(String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return Optional.empty();
        }

        String redisKey = redisKey(idempotencyKey);
        String currentValue = redisTemplate.opsForValue().get(redisKey);
        if (currentValue != null) {
            return handleCurrentValue(idempotencyKey, currentValue);
        }

        Boolean started = redisTemplate.opsForValue().setIfAbsent(redisKey, IN_PROGRESS, properties.orderIdempotencyTtl());
        if (Boolean.TRUE.equals(started)) {
            log.debug("Order idempotency key reserved idempotencyKey={}", idempotencyKey);
            return Optional.empty();
        }

        String valueAfterRace = redisTemplate.opsForValue().get(redisKey);
        if (valueAfterRace != null) {
            return handleCurrentValue(idempotencyKey, valueAfterRace);
        }

        throw new DuplicateOrderRequestException("Order request is already being processed for idempotency key: " + idempotencyKey);
    }

    /**
     * Marks an idempotency key as completed with a created order ID.
     *
     * @param idempotencyKey client-provided idempotency key
     * @param orderId created order ID
     */
    public void complete(String idempotencyKey, String orderId) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return;
        }
        redisTemplate.opsForValue().set(redisKey(idempotencyKey), ORDER_PREFIX + orderId, properties.orderIdempotencyTtl());
        log.debug("Order idempotency key completed idempotencyKey={} orderId={}", idempotencyKey, orderId);
    }

    /**
     * Removes an in-progress marker after a failed order placement attempt.
     *
     * @param idempotencyKey client-provided idempotency key
     */
    public void clearIfInProgress(String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return;
        }

        String redisKey = redisKey(idempotencyKey);
        String currentValue = redisTemplate.opsForValue().get(redisKey);
        if (IN_PROGRESS.equals(currentValue)) {
            redisTemplate.delete(redisKey);
            log.debug("Order idempotency key cleared idempotencyKey={}", idempotencyKey);
        }
    }

    /**
     * Handles an existing Redis value for an idempotency key.
     *
     * @param idempotencyKey client-provided idempotency key
     * @param currentValue current Redis value
     * @return existing order ID when available
     */
    private Optional<String> handleCurrentValue(String idempotencyKey, String currentValue) {
        if (currentValue.startsWith(ORDER_PREFIX)) {
            String orderId = currentValue.substring(ORDER_PREFIX.length());
            log.info("Order idempotency key reused idempotencyKey={} orderId={}", idempotencyKey, orderId);
            return Optional.of(orderId);
        }

        throw new DuplicateOrderRequestException("Order request is already being processed for idempotency key: " + idempotencyKey);
    }

    /**
     * Converts a client idempotency key into a namespaced Redis key.
     *
     * @param idempotencyKey client-provided idempotency key
     * @return Redis key
     */
    private String redisKey(String idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }
}
