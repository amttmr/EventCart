package com.eventcart.payment.service;

import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.payment.config.PaymentSimulationProperties;
import com.eventcart.payment.domain.PaymentAttemptDocument;
import com.eventcart.payment.domain.PaymentStatus;
import com.eventcart.payment.dto.PaymentAttemptResponse;
import com.eventcart.payment.event.PaymentEventPublisher;
import com.eventcart.payment.exception.PaymentAttemptNotFoundException;
import com.eventcart.payment.mapper.PaymentMapper;
import com.eventcart.payment.repository.PaymentAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service that owns mock payment processing.
 */
@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentSimulationProperties simulationProperties;

    /**
     * Creates a payment service.
     *
     * @param paymentAttemptRepository repository for payment attempts
     * @param paymentMapper mapper between payment documents, DTOs, and events
     * @param eventPublisher Kafka publisher for payment result events
     * @param simulationProperties mock provider behavior configuration
     */
    public PaymentService(
            PaymentAttemptRepository paymentAttemptRepository,
            PaymentMapper paymentMapper,
            PaymentEventPublisher eventPublisher,
            PaymentSimulationProperties simulationProperties
    ) {
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentMapper = paymentMapper;
        this.eventPublisher = eventPublisher;
        this.simulationProperties = simulationProperties;
    }

    /**
     * Processes a payment after inventory has been reserved.
     *
     * @param event inventory-reserved event consumed from Kafka
     * @return payment attempt response
     */
    public PaymentAttemptResponse processInventoryReserved(InventoryReservedEvent event) {
        log.info("Processing payment orderId={} customerId={} amount={} currency={}",
                event.orderId(), event.customerId(), event.totalAmount(), event.currency());
        Optional<PaymentAttemptDocument> existingAttempt = paymentAttemptRepository.findByOrderId(event.orderId());
        if (existingAttempt.isPresent()) {
            log.info("Skipping duplicate payment processing orderId={} paymentId={} status={}",
                    event.orderId(), existingAttempt.get().getId(), existingAttempt.get().getStatus());
            return paymentMapper.toResponse(existingAttempt.get());
        }

        PaymentAttemptDocument attempt = new PaymentAttemptDocument();
        attempt.setOrderId(event.orderId());
        attempt.setCustomerId(event.customerId());
        attempt.setAmount(event.totalAmount());
        attempt.setCurrency(event.currency());
        attempt.setProviderName(simulationProperties.providerName());

        Optional<String> failureReason = validatePayment(event.totalAmount());
        if (failureReason.isPresent()) {
            attempt.setStatus(PaymentStatus.FAILED);
            attempt.setFailureReason(failureReason.get());
            PaymentAttemptDocument savedAttempt = paymentAttemptRepository.save(attempt);
            log.warn("Payment failed orderId={} paymentId={} reason={}",
                    event.orderId(), savedAttempt.getId(), failureReason.get());
            eventPublisher.publishPaymentFailed(paymentMapper.toPaymentFailedEvent(savedAttempt));
            return paymentMapper.toResponse(savedAttempt);
        }

        attempt.setStatus(PaymentStatus.COMPLETED);
        attempt.setProviderTransactionId(simulationProperties.providerName() + "-" + UUID.randomUUID());
        PaymentAttemptDocument savedAttempt = paymentAttemptRepository.save(attempt);
        log.info("Payment completed orderId={} paymentId={} providerTransactionId={}",
                event.orderId(), savedAttempt.getId(), savedAttempt.getProviderTransactionId());
        eventPublisher.publishPaymentCompleted(paymentMapper.toPaymentCompletedEvent(savedAttempt));
        return paymentMapper.toResponse(savedAttempt);
    }

    /**
     * Retrieves the payment attempt for one order.
     *
     * @param orderId order ID
     * @return payment attempt response
     */
    public PaymentAttemptResponse getPaymentAttemptForOrder(String orderId) {
        log.debug("Fetching payment attempt orderId={}", orderId);
        return paymentMapper.toResponse(paymentAttemptRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment attempt not found orderId={}", orderId);
                    return new PaymentAttemptNotFoundException("Payment attempt not found for order: " + orderId);
                }));
    }

    /**
     * Applies deterministic mock provider validation.
     *
     * @param amount attempted payment amount
     * @return optional failure reason
     */
    private Optional<String> validatePayment(BigDecimal amount) {
        if (amount == null) {
            return Optional.of("Payment amount is missing");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("Payment amount must be greater than zero");
        }
        if (amount.compareTo(simulationProperties.failureAmountThreshold()) >= 0) {
            return Optional.of("Mock payment declined because amount " + amount
                    + " is greater than or equal to threshold " + simulationProperties.failureAmountThreshold());
        }
        return Optional.empty();
    }
}
