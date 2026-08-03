package com.eventcart.payment.service;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.common.events.InventoryReservedItem;
import com.eventcart.common.security.CustomerAccessPolicy;
import com.eventcart.payment.config.PaymentSimulationProperties;
import com.eventcart.payment.domain.PaymentAttemptDocument;
import com.eventcart.payment.domain.PaymentStatus;
import com.eventcart.payment.dto.PaymentAttemptResponse;
import com.eventcart.payment.mapper.PaymentMapper;
import com.eventcart.payment.outbox.PaymentOutboxService;
import com.eventcart.payment.repository.PaymentAttemptRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PaymentService}.
 */
class PaymentServiceTest {
    private final PaymentAttemptRepository paymentAttemptRepository = mock(PaymentAttemptRepository.class);
    private final PaymentMapper paymentMapper = new PaymentMapper();
    private final PaymentOutboxService outboxService = mock(PaymentOutboxService.class);
    private final PaymentSimulationProperties simulationProperties =
            new PaymentSimulationProperties("MockPay", new BigDecimal("50000.00"));
    private final CustomerAccessPolicy customerAccessPolicy = new CustomerAccessPolicy(false);
    private final PaymentService paymentService = new PaymentService(
            paymentAttemptRepository,
            paymentMapper,
            outboxService,
            simulationProperties,
            customerAccessPolicy
    );

    /**
     * Verifies that payment succeeds below the configured failure threshold.
     */
    @Test
    void processInventoryReservedShouldCompletePaymentBelowThreshold() {
        when(paymentAttemptRepository.findByOrderId("order-1")).thenReturn(Optional.empty());
        when(paymentAttemptRepository.save(any(PaymentAttemptDocument.class))).thenAnswer(invocation -> {
            PaymentAttemptDocument attempt = invocation.getArgument(0);
            attempt.setId("payment-1");
            return attempt;
        });

        PaymentAttemptResponse response = paymentService.processInventoryReserved(inventoryReservedEvent(new BigDecimal("6999.00")));

        assertThat(response.paymentId()).isEqualTo("payment-1");
        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.providerTransactionId()).startsWith("MockPay-");
        verify(outboxService).enqueuePaymentCompleted(any());
        verify(outboxService, never()).enqueuePaymentFailed(any());
    }

    /**
     * Verifies that payment fails at or above the configured failure threshold.
     */
    @Test
    void processInventoryReservedShouldFailPaymentAtThreshold() {
        when(paymentAttemptRepository.findByOrderId("order-1")).thenReturn(Optional.empty());
        when(paymentAttemptRepository.save(any(PaymentAttemptDocument.class))).thenAnswer(invocation -> {
            PaymentAttemptDocument attempt = invocation.getArgument(0);
            attempt.setId("payment-1");
            return attempt;
        });

        PaymentAttemptResponse response = paymentService.processInventoryReserved(inventoryReservedEvent(new BigDecimal("50000.00")));

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.failureReason()).contains("Mock payment declined");
        verify(outboxService).enqueuePaymentFailed(any());
        verify(outboxService, never()).enqueuePaymentCompleted(any());
    }

    /**
     * Verifies that duplicate inventory-reserved events do not create a second payment attempt.
     */
    @Test
    void processInventoryReservedShouldSkipDuplicatePaymentAttempt() {
        PaymentAttemptDocument existingAttempt = new PaymentAttemptDocument();
        existingAttempt.setId("payment-1");
        existingAttempt.setOrderId("order-1");
        existingAttempt.setCustomerId("customer-1");
        existingAttempt.setStatus(PaymentStatus.COMPLETED);
        existingAttempt.setAmount(new BigDecimal("6999.00"));
        existingAttempt.setCurrency("INR");
        when(paymentAttemptRepository.findByOrderId("order-1")).thenReturn(Optional.of(existingAttempt));

        PaymentAttemptResponse response = paymentService.processInventoryReserved(inventoryReservedEvent(new BigDecimal("6999.00")));

        assertThat(response.paymentId()).isEqualTo("payment-1");
        verify(paymentAttemptRepository, never()).save(any());
        verify(outboxService, never()).enqueuePaymentCompleted(any());
        verify(outboxService, never()).enqueuePaymentFailed(any());
    }

    /**
     * Creates an inventory-reserved event for service tests.
     *
     * @param amount order amount
     * @return inventory-reserved event
     */
    private InventoryReservedEvent inventoryReservedEvent(BigDecimal amount) {
        return new InventoryReservedEvent(
                EventMetadata.create(InventoryReservedEvent.EVENT_TYPE, InventoryReservedEvent.VERSION, "order-1"),
                "order-1",
                "customer-1",
                List.of(new InventoryReservedItem("product-1", "SKU-1", 1)),
                amount,
                "INR"
        );
    }
}
