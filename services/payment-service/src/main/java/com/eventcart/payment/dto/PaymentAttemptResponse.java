package com.eventcart.payment.dto;

import com.eventcart.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Public representation of a payment attempt.
 *
 * @param paymentId MongoDB-generated payment attempt ID
 * @param orderId order ID this payment attempt belongs to
 * @param customerId customer that placed the order
 * @param status payment attempt status
 * @param amount attempted payment amount
 * @param currency payment currency code
 * @param providerName mock payment provider name
 * @param providerTransactionId provider transaction ID for completed payments
 * @param failureReason failure reason for failed payments
 * @param version optimistic locking version maintained by MongoDB/Spring Data
 * @param createdAt time when the payment attempt was created
 * @param updatedAt time when the payment attempt was last modified
 */
public record PaymentAttemptResponse(
        String paymentId,
        String orderId,
        String customerId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String providerName,
        String providerTransactionId,
        String failureReason,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
