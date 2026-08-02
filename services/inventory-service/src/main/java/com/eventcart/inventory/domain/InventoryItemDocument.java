package com.eventcart.inventory.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document that represents inventory stock for one product.
 */
@Document(collection = "inventory_items")
public class InventoryItemDocument {
    @Id
    private String productId;

    @Indexed
    private String sku;

    private String productName;

    private int availableQuantity;

    private int reservedQuantity;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns the product ID used as the inventory document ID.
     *
     * @return product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the product ID used as the inventory document ID.
     *
     * @param productId product ID
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the product SKU snapshot.
     *
     * @return product SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Sets the product SKU snapshot.
     *
     * @param sku product SKU
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Returns the product display name snapshot.
     *
     * @return product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product display name snapshot.
     *
     * @param productName product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns quantity available for new reservations.
     *
     * @return available quantity
     */
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Sets quantity available for new reservations.
     *
     * @param availableQuantity available quantity
     */
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    /**
     * Returns quantity already reserved for orders.
     *
     * @return reserved quantity
     */
    public int getReservedQuantity() {
        return reservedQuantity;
    }

    /**
     * Sets quantity already reserved for orders.
     *
     * @param reservedQuantity reserved quantity
     */
    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
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
     * Returns when the inventory item was created.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the inventory item was created.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns when the inventory item was last updated.
     *
     * @return last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the inventory item was last updated.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
