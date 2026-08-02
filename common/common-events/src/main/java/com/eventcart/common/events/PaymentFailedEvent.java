package com.eventcart.common.events;

import java.math.BigDecimal;

/**
 * Kafka event published when payment-service cannot complete a payment.
 *
 * @param metadata common event metadata used for traceability and versioning
 * @param paymentId payment attempt ID
 * @param orderId order ID whose payment failed
 * @param customerId customer that placed the order
 * @param amount attempted payment amount
 * @param currency payment currency code
 * @param reason human-readable failure reason
 */
public record PaymentFailedEvent(
        EventMetadata metadata,
        String paymentId,
        String orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        String reason
) {
    public static final String EVENT_TYPE = "payment.failed";
    public static final int VERSION = 1;
}
