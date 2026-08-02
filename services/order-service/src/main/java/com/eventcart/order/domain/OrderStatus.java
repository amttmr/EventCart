package com.eventcart.order.domain;

/**
 * Current lifecycle status of an order.
 */
public enum OrderStatus {
    CREATED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED
}
