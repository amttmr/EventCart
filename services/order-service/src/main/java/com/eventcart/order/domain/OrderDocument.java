package com.eventcart.order.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document that represents an order owned by order-service.
 */
@Document(collection = "orders")
public class OrderDocument {
    @Id
    private String id;

    @Indexed
    private String customerId;

    private List<OrderItemDocument> items = new ArrayList<>();

    private BigDecimal totalAmount = BigDecimal.ZERO;

    private String currency = "INR";

    @Indexed
    private OrderStatus status = OrderStatus.CREATED;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns the MongoDB-generated order ID.
     *
     * @return order ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the MongoDB-generated order ID.
     *
     * @param id order ID
     */
    public void setId(String id) {
        this.id = id;
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
     * Returns immutable order item snapshots.
     *
     * @return order items
     */
    public List<OrderItemDocument> getItems() {
        return items;
    }

    /**
     * Sets immutable order item snapshots.
     *
     * @param items order items
     */
    public void setItems(List<OrderItemDocument> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    /**
     * Returns the order total amount.
     *
     * @return total amount
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets the order total amount.
     *
     * @param totalAmount total amount
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Returns the order currency.
     *
     * @return currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the order currency.
     *
     * @param currency currency code
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the order lifecycle status.
     *
     * @return order status
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Sets the order lifecycle status.
     *
     * @param status order status
     */
    public void setStatus(OrderStatus status) {
        this.status = status;
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
     * Returns when the order was created.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the order was created.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns when the order was last updated.
     *
     * @return last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the order was last updated.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
