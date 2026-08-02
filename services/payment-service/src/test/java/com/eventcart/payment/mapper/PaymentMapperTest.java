package com.eventcart.payment.mapper;

import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.payment.domain.PaymentAttemptDocument;
import com.eventcart.payment.domain.PaymentStatus;
import com.eventcart.payment.dto.PaymentAttemptResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PaymentMapper}.
 */
class PaymentMapperTest {
    private final PaymentMapper paymentMapper = new PaymentMapper();

    /**
     * Verifies that a payment attempt becomes a public API response.
     */
    @Test
    void toResponseShouldMapPaymentAttempt() {
        PaymentAttemptResponse response = paymentMapper.toResponse(completedAttempt());

        assertThat(response.paymentId()).isEqualTo("payment-1");
        assertThat(response.orderId()).isEqualTo("order-1");
        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
    }

    /**
     * Verifies that a completed payment attempt becomes a payment-completed event.
     */
    @Test
    void toPaymentCompletedEventShouldMapAttempt() {
        PaymentCompletedEvent event = paymentMapper.toPaymentCompletedEvent(completedAttempt());

        assertThat(event.metadata().eventType()).isEqualTo(PaymentCompletedEvent.EVENT_TYPE);
        assertThat(event.paymentId()).isEqualTo("payment-1");
        assertThat(event.amount()).isEqualByComparingTo("6999.00");
    }

    /**
     * Verifies that a failed payment attempt becomes a payment-failed event.
     */
    @Test
    void toPaymentFailedEventShouldMapAttempt() {
        PaymentAttemptDocument attempt = completedAttempt();
        attempt.setStatus(PaymentStatus.FAILED);
        attempt.setFailureReason("Mock payment declined");

        PaymentFailedEvent event = paymentMapper.toPaymentFailedEvent(attempt);

        assertThat(event.metadata().eventType()).isEqualTo(PaymentFailedEvent.EVENT_TYPE);
        assertThat(event.reason()).isEqualTo("Mock payment declined");
    }

    /**
     * Creates a completed payment attempt document for mapper tests.
     *
     * @return payment attempt document
     */
    private PaymentAttemptDocument completedAttempt() {
        PaymentAttemptDocument attempt = new PaymentAttemptDocument();
        attempt.setId("payment-1");
        attempt.setOrderId("order-1");
        attempt.setCustomerId("customer-1");
        attempt.setStatus(PaymentStatus.COMPLETED);
        attempt.setAmount(new BigDecimal("6999.00"));
        attempt.setCurrency("INR");
        attempt.setProviderName("MockPay");
        attempt.setProviderTransactionId("MockPay-transaction-1");
        return attempt;
    }
}
