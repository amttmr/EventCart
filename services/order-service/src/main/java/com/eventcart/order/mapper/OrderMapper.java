package com.eventcart.order.mapper;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.events.OrderCreatedItem;
import com.eventcart.order.client.CartItemResponse;
import com.eventcart.order.client.CartResponse;
import com.eventcart.order.domain.OrderDocument;
import com.eventcart.order.domain.OrderItemDocument;
import com.eventcart.order.dto.OrderItemResponse;
import com.eventcart.order.dto.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps cart responses, order documents, API DTOs, and Kafka event payloads.
 */
@Component
public class OrderMapper {
    /**
     * Converts a cart-service response into a new order document.
     *
     * @param cart cart data fetched from cart-service
     * @return unsaved order document
     */
    public OrderDocument toDocument(CartResponse cart) {
        OrderDocument order = new OrderDocument();
        order.setCustomerId(cart.customerId());
        order.setItems(cart.items()
                .stream()
                .map(this::toOrderItemDocument)
                .toList());
        order.setTotalAmount(cart.subtotal());
        order.setCurrency(cart.currency());
        return order;
    }

    /**
     * Converts an order document into a public API response.
     *
     * @param order persisted order document
     * @return public order response
     */
    public OrderResponse toResponse(OrderDocument order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getItems().stream().map(this::toOrderItemResponse).toList(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getStatus(),
                order.getStatusReason(),
                order.getVersion(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    /**
     * Converts an order document into the Kafka event consumed by inventory-service.
     *
     * @param order persisted order document
     * @return order-created event
     */
    public OrderCreatedEvent toOrderCreatedEvent(OrderDocument order) {
        return new OrderCreatedEvent(
                EventMetadata.create(OrderCreatedEvent.EVENT_TYPE, OrderCreatedEvent.VERSION, order.getId()),
                order.getId(),
                order.getCustomerId(),
                order.getItems().stream().map(this::toOrderCreatedItem).toList(),
                order.getTotalAmount(),
                order.getCurrency()
        );
    }

    /**
     * Converts customer order documents into public API responses.
     *
     * @param orders persisted order documents
     * @return public order responses
     */
    public List<OrderResponse> toResponses(List<OrderDocument> orders) {
        return orders.stream().map(this::toResponse).toList();
    }

    /**
     * Converts one cart item into an embedded order item snapshot.
     *
     * @param item cart item returned by cart-service
     * @return embedded order item document
     */
    private OrderItemDocument toOrderItemDocument(CartItemResponse item) {
        return new OrderItemDocument(
                item.productId(),
                item.sku(),
                item.productName(),
                item.unitPrice(),
                item.currency(),
                item.quantity(),
                item.lineTotal()
        );
    }

    /**
     * Converts one order item document into an API item response.
     *
     * @param item embedded order item document
     * @return public order item response
     */
    private OrderItemResponse toOrderItemResponse(OrderItemDocument item) {
        return new OrderItemResponse(
                item.productId(),
                item.sku(),
                item.productName(),
                item.unitPrice(),
                item.currency(),
                item.quantity(),
                item.lineTotal()
        );
    }

    /**
     * Converts one order item document into an order-created event item.
     *
     * @param item embedded order item document
     * @return event item snapshot
     */
    private OrderCreatedItem toOrderCreatedItem(OrderItemDocument item) {
        return new OrderCreatedItem(
                item.productId(),
                item.sku(),
                item.productName(),
                item.unitPrice(),
                item.currency(),
                item.quantity(),
                item.lineTotal()
        );
    }
}
