package com.eventcart.catalog.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document that represents a product owned by catalog-service.
 *
 * <p>The document contains product data needed for browsing and product
 * administration. Order history will store its own product snapshot later so
 * old orders are not affected when this document changes.</p>
 */
@Document(collection = "products")
@CompoundIndex(name = "category_active_idx", def = "{'category': 1, 'active': 1}")
public class ProductDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    @Indexed
    private String name;

    private String description;

    @Indexed
    private String category;

    private BigDecimal price;

    private String currency;

    private int availableQuantity;

    private List<String> tags = new ArrayList<>();

    @Indexed
    private boolean active = true;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Returns the MongoDB-generated product ID.
     *
     * @return product ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the MongoDB-generated product ID.
     *
     * @param id product ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the stock keeping unit.
     *
     * @return unique product SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Sets the stock keeping unit.
     *
     * @param sku unique product SKU
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Returns the product display name.
     *
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product display name.
     *
     * @param name product name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the product description.
     *
     * @return product description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the product description.
     *
     * @param description product description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the product category.
     *
     * @return product category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the product category.
     *
     * @param category product category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the current product price.
     *
     * @return product price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the current product price.
     *
     * @param price product price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the product currency code.
     *
     * @return currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the product currency code.
     *
     * @param currency currency code
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the current available quantity.
     *
     * @return available quantity
     */
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Sets the current available quantity.
     *
     * @param availableQuantity available quantity
     */
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    /**
     * Returns searchable product tags.
     *
     * @return product tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets searchable product tags.
     *
     * @param tags product tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    /**
     * Returns whether this product is active.
     *
     * @return {@code true} when the product is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether this product is active.
     *
     * @param active active flag
     */
    public void setActive(boolean active) {
        this.active = active;
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
     * Returns when the product was created.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the product was created.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns when the product was last updated.
     *
     * @return last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the product was last updated.
     *
     * @param updatedAt last update timestamp
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
