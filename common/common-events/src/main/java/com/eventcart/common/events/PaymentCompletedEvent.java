package com.eventcart.common.events;

import java.math.BigDecimal;

/**
 * Kafka event published when payment-service successfully captures a payment.
 *
 * @param metadata common event metadata used for traceability and versioning
 * @param paymentId payment attempt ID
 * @param orderId order ID whose payment was completed
 * @param customerId customer that placed the order
 * @param amount captured payment amount
 * @param currency payment currency code
 * @param providerTransactionId mock provider transaction identifier
 */
public record PaymentCompletedEvent(
        EventMetadata metadata,
        String paymentId,
        String orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        String providerTransactionId
) {
    public static final String EVENT_TYPE = "payment.completed";
    public static final int VERSION = 1;
}
