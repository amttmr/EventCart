package com.eventcart.inventory.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document that stores the result of reserving stock for an order.
 */
@Document(collection = "inventory_reservations")
public class InventoryReservationDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String orderId;

    @Indexed
    private String customerId;

    @Indexed
    private InventoryReservationStatus status;

    private List<InventoryReservationItemDocument> items = new ArrayList<>();

    private String failureReason;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns the MongoDB-generated reservation ID.
     *
     * @return reservation ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the MongoDB-generated reservation ID.
     *
     * @param id reservation ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the order ID this reservation belongs to.
     *
     * @return order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID this reservation belongs to.
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
     * Returns the reservation status.
     *
     * @return reservation status
     */
    public InventoryReservationStatus getStatus() {
        return status;
    }

    /**
     * Sets the reservation status.
     *
     * @param status reservation status
     */
    public void setStatus(InventoryReservationStatus status) {
        this.status = status;
    }

    /**
     * Returns reserved item quantities.
     *
     * @return reservation items
     */
    public List<InventoryReservationItemDocument> getItems() {
        return items;
    }

    /**
     * Sets reserved item quantities.
     *
     * @param items reservation items
     */
    public void setItems(List<InventoryReservationItemDocument> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    /**
     * Returns the failure reason for failed reservations.
     *
     * @return failure reason
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Sets the failure reason for failed reservations.
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
     * Returns when the reservation was created.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the reservation was created.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns when the reservation was last updated.
     *
     * @return last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the reservation was last updated.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
