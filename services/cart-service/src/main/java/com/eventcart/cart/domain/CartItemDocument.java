package com.eventcart.cart.domain;

import java.math.BigDecimal;

/**
 * Embedded MongoDB document that represents one item inside a cart.
 *
 * <p>The item stores a product snapshot instead of only a product ID. This
 * keeps the cart readable even if catalog data changes between add-to-cart and
 * checkout.</p>
 */
public class CartItemDocument {
    private String productId;
    private String sku;
    private String productName;
    private BigDecimal unitPrice;
    private String currency;
    private int quantity;

    /**
     * Returns the catalog product ID.
     *
     * @return product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the catalog product ID.
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
     * Returns the product name snapshot.
     *
     * @return product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product name snapshot.
     *
     * @param productName product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns the unit price snapshot.
     *
     * @return unit price
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Sets the unit price snapshot.
     *
     * @param unitPrice unit price
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * Returns the currency code.
     *
     * @return currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency code.
     *
     * @param currency currency code
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the quantity of this item in the cart.
     *
     * @return item quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of this item in the cart.
     *
     * @param quantity item quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

