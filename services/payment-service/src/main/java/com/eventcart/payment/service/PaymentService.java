package com.eventcart.payment.service;

import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.common.security.CustomerAccessPolicy;
import com.eventcart.payment.config.PaymentSimulationProperties;
import com.eventcart.payment.domain.PaymentAttemptDocument;
import com.eventcart.payment.domain.PaymentStatus;
import com.eventcart.payment.dto.PaymentAttemptResponse;
import com.eventcart.payment.exception.PaymentAttemptNotFoundException;
import com.eventcart.payment.mapper.PaymentMapper;
import com.eventcart.payment.outbox.PaymentOutboxService;
import com.eventcart.payment.repository.PaymentAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PaymentOutboxService outboxService;
    private final PaymentSimulationProperties simulationProperties;
    private final CustomerAccessPolicy customerAccessPolicy;

    /**
     * Creates a payment service.
     *
     * @param paymentAttemptRepository repository for payment attempts
     * @param paymentMapper mapper between payment documents, DTOs, and events
     * @param outboxService outbox service for reliable payment result publishing
     * @param simulationProperties mock provider behavior configuration
     * @param customerAccessPolicy ownership policy for customer-scoped lookups
     */
    public PaymentService(
            PaymentAttemptRepository paymentAttemptRepository,
            PaymentMapper paymentMapper,
            PaymentOutboxService outboxService,
            PaymentSimulationProperties simulationProperties,
            CustomerAccessPolicy customerAccessPolicy
    ) {
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentMapper = paymentMapper;
        this.outboxService = outboxService;
        this.simulationProperties = simulationProperties;
        this.customerAccessPolicy = customerAccessPolicy;
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
            outboxService.enqueuePaymentFailed(paymentMapper.toPaymentFailedEvent(savedAttempt));
            return paymentMapper.toResponse(savedAttempt);
        }

        attempt.setStatus(PaymentStatus.COMPLETED);
        attempt.setProviderTransactionId(simulationProperties.providerName() + "-" + UUID.randomUUID());
        PaymentAttemptDocument savedAttempt = paymentAttemptRepository.save(attempt);
        log.info("Payment completed orderId={} paymentId={} providerTransactionId={}",
                event.orderId(), savedAttempt.getId(), savedAttempt.getProviderTransactionId());
        outboxService.enqueuePaymentCompleted(paymentMapper.toPaymentCompletedEvent(savedAttempt));
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
        PaymentAttemptDocument attempt = paymentAttemptRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment attempt not found orderId={}", orderId);
                    return new PaymentAttemptNotFoundException("Payment attempt not found for order: " + orderId);
                });
        customerAccessPolicy.requireCustomerAccess(
                attempt.getCustomerId(),
                SecurityContextHolder.getContext().getAuthentication()
        );
        return paymentMapper.toResponse(attempt);
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
