package com.eventcart.cart.domain;

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
 * MongoDB document that represents a customer's active shopping cart.
 *
 * <p>A cart embeds its items because the service usually reads and writes the
 * cart as one aggregate. This is a common MongoDB modeling choice for
 * parent-child data that is loaded together.</p>
 */
@Document(collection = "carts")
public class CartDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String customerId;

    private List<CartItemDocument> items = new ArrayList<>();

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns the MongoDB-generated cart ID.
     *
     * @return cart ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the MongoDB-generated cart ID.
     *
     * @param id cart ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the customer that owns the cart.
     *
     * @return customer ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer that owns the cart.
     *
     * @param customerId customer ID
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the embedded cart items.
     *
     * @return mutable list of cart items
     */
    public List<CartItemDocument> getItems() {
        return items;
    }

    /**
     * Sets the embedded cart items.
     *
     * @param items cart items
     */
    public void setItems(List<CartItemDocument> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
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
     * Returns when the cart was created.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the cart was created.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns when the cart was last updated.
     *
     * @return last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the cart was last updated.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

