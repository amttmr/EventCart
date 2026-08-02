package com.eventcart.payment.repository;

import com.eventcart.payment.domain.PaymentAttemptDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * MongoDB repository for payment attempts.
 */
public interface PaymentAttemptRepository extends MongoRepository<PaymentAttemptDocument, String> {
    /**
     * Finds the payment attempt for one order.
     *
     * @param orderId order ID
     * @return optional payment attempt
     */
    Optional<PaymentAttemptDocument> findByOrderId(String orderId);
}
