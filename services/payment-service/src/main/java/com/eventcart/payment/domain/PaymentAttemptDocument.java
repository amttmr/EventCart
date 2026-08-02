package com.eventcart.payment.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MongoDB document that stores one mock payment attempt for an order.
 */
@Document(collection = "payment_attempts")
public class PaymentAttemptDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String orderId;

    @Indexed
    private String customerId;

    @Indexed
    private PaymentStatus status;

    private BigDecimal amount = BigDecimal.ZERO;

    private String currency = "INR";

    private String providerName;

    private String providerTransactionId;

    private String failureReason;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns the MongoDB-generated payment attempt ID.
     *
     * @return payment attempt ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the MongoDB-generated payment attempt ID.
     *
     * @param id payment attempt ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the order ID this payment attempt belongs to.
     *
     * @return order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID this payment attempt belongs to.
     *
     * @param orderId order ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Returns the customer that placed the order.
     *
     * @return customer ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer that placed the order.
     *
     * @param customerId customer ID
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the payment attempt status.
     *
     * @return payment status
     */
    public PaymentStatus getStatus() {
        return status;
    }

    /**
     * Sets the payment attempt status.
     *
     * @param status payment status
     */
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    /**
     * Returns the attempted payment amount.
     *
     * @return payment amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the attempted payment amount.
     *
     * @param amount payment amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Returns the payment currency code.
     *
     * @return currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the payment currency code.
     *
     * @param currency currency code
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the mock provider name.
     *
     * @return provider name
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Sets the mock provider name.
     *
     * @param providerName provider name
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * Returns the provider transaction ID for completed payments.
     *
     * @return provider transaction ID
     */
    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    /**
     * Sets the provider transaction ID for completed payments.
     *
     * @param providerTransactionId provider transaction ID
     */
    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }

    /**
     * Returns the failure reason for failed payments.
     *
     * @return failure reason
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Sets the failure reason for failed payments.
     *
     * @param failureReason failure reason
     */
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * Returns the optimistic locking version.
     *
     * @return document version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the optimistic locking version.
     *
     * @param version document version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Returns when the payment attempt was created.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the payment attempt was created.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns when the payment attempt was last updated.
     *
     * @return last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the payment attempt was last updated.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
