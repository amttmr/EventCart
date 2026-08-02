package com.eventcart.payment.mapper;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.common.web.observability.CorrelationIdContext;
import com.eventcart.payment.domain.PaymentAttemptDocument;
import com.eventcart.payment.dto.PaymentAttemptResponse;
import org.springframework.stereotype.Component;

/**
 * Maps payment documents, API DTOs, and Kafka event payloads.
 */
@Component
public class PaymentMapper {
    /**
     * Converts a payment document into a public API response.
     *
     * @param attempt persisted payment attempt
     * @return public payment attempt response
     */
    public PaymentAttemptResponse toResponse(PaymentAttemptDocument attempt) {
        return new PaymentAttemptResponse(
                attempt.getId(),
                attempt.getOrderId(),
                attempt.getCustomerId(),
                attempt.getStatus(),
                attempt.getAmount(),
                attempt.getCurrency(),
                attempt.getProviderName(),
                attempt.getProviderTransactionId(),
                attempt.getFailureReason(),
                attempt.getVersion(),
                attempt.getCreatedAt(),
                attempt.getUpdatedAt()
        );
    }

    /**
     * Converts a completed payment attempt into a Kafka event.
     *
     * @param attempt persisted payment attempt
     * @return payment-completed event
     */
    public PaymentCompletedEvent toPaymentCompletedEvent(PaymentAttemptDocument attempt) {
        return new PaymentCompletedEvent(
                EventMetadata.create(
                        PaymentCompletedEvent.EVENT_TYPE,
                        PaymentCompletedEvent.VERSION,
                        CorrelationIdContext.getCorrelationIdOr(attempt.getOrderId())
                ),
                attempt.getId(),
                attempt.getOrderId(),
                attempt.getCustomerId(),
                attempt.getAmount(),
                attempt.getCurrency(),
                attempt.getProviderTransactionId()
        );
    }

    /**
     * Converts a failed payment attempt into a Kafka event.
     *
     * @param attempt persisted payment attempt
     * @return payment-failed event
     */
    public PaymentFailedEvent toPaymentFailedEvent(PaymentAttemptDocument attempt) {
        return new PaymentFailedEvent(
                EventMetadata.create(
                        PaymentFailedEvent.EVENT_TYPE,
                        PaymentFailedEvent.VERSION,
                        CorrelationIdContext.getCorrelationIdOr(attempt.getOrderId())
                ),
                attempt.getId(),
                attempt.getOrderId(),
                attempt.getCustomerId(),
                attempt.getAmount(),
                attempt.getCurrency(),
                attempt.getFailureReason()
        );
    }
}
