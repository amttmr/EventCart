package com.eventcart.inventory.mapper;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.common.events.InventoryReservedItem;
import com.eventcart.common.events.OrderCreatedItem;
import com.eventcart.inventory.domain.InventoryItemDocument;
import com.eventcart.inventory.domain.InventoryReservationDocument;
import com.eventcart.inventory.domain.InventoryReservationItemDocument;
import com.eventcart.inventory.dto.InventoryItemResponse;
import com.eventcart.inventory.dto.InventoryReservationItemResponse;
import com.eventcart.inventory.dto.InventoryReservationResponse;
import com.eventcart.inventory.dto.UpsertInventoryItemRequest;
import org.springframework.stereotype.Component;

/**
 * Maps inventory documents, API DTOs, and Kafka event payloads.
 */
@Component
public class InventoryMapper {
    /**
     * Applies an upsert request to an inventory item document.
     *
     * @param productId product ID from the path
     * @param request validated upsert request
     * @param item inventory document to update
     */
    public void updateItemDocument(String productId, UpsertInventoryItemRequest request, InventoryItemDocument item) {
        item.setProductId(productId);
        item.setSku(request.sku());
        item.setProductName(request.productName());
        item.setAvailableQuantity(request.availableQuantity());
    }

    /**
     * Converts an inventory item document into a public API response.
     *
     * @param item persisted inventory item
     * @return public inventory item response
     */
    public InventoryItemResponse toItemResponse(InventoryItemDocument item) {
        return new InventoryItemResponse(
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getAvailableQuantity(),
                item.getReservedQuantity(),
                item.getVersion(),
                item.getUpdatedAt()
        );
    }

    /**
     * Converts an order-created item into an embedded reservation item.
     *
     * @param item ordered item from the Kafka event
     * @return embedded reservation item document
     */
    public InventoryReservationItemDocument toReservationItem(OrderCreatedItem item) {
        return new InventoryReservationItemDocument(item.productId(), item.sku(), item.quantity());
    }

    /**
     * Converts a reservation document into a public API response.
     *
     * @param reservation persisted reservation document
     * @return public reservation response
     */
    public InventoryReservationResponse toReservationResponse(InventoryReservationDocument reservation) {
        return new InventoryReservationResponse(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getCustomerId(),
                reservation.getStatus(),
                reservation.getItems().stream().map(this::toReservationItemResponse).toList(),
                reservation.getFailureReason(),
                reservation.getVersion(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }

    /**
     * Converts a successful reservation document into a Kafka event.
     *
     * @param reservation persisted reservation document
     * @return inventory-reserved event
     */
    public InventoryReservedEvent toInventoryReservedEvent(InventoryReservationDocument reservation) {
        return new InventoryReservedEvent(
                EventMetadata.create(InventoryReservedEvent.EVENT_TYPE, InventoryReservedEvent.VERSION, reservation.getOrderId()),
                reservation.getOrderId(),
                reservation.getCustomerId(),
                reservation.getItems().stream().map(this::toInventoryReservedItem).toList()
        );
    }

    /**
     * Converts a failed reservation document into a Kafka event.
     *
     * @param reservation persisted reservation document
     * @return inventory-reservation-failed event
     */
    public InventoryReservationFailedEvent toInventoryReservationFailedEvent(InventoryReservationDocument reservation) {
        return new InventoryReservationFailedEvent(
                EventMetadata.create(
                        InventoryReservationFailedEvent.EVENT_TYPE,
                        InventoryReservationFailedEvent.VERSION,
                        reservation.getOrderId()
                ),
                reservation.getOrderId(),
                reservation.getCustomerId(),
                reservation.getFailureReason()
        );
    }

    /**
     * Converts one reservation item document into an API item response.
     *
     * @param item embedded reservation item document
     * @return public reservation item response
     */
    private InventoryReservationItemResponse toReservationItemResponse(InventoryReservationItemDocument item) {
        return new InventoryReservationItemResponse(item.productId(), item.sku(), item.quantity());
    }

    /**
     * Converts one reservation item document into an event item.
     *
     * @param item embedded reservation item document
     * @return event item
     */
    private InventoryReservedItem toInventoryReservedItem(InventoryReservationItemDocument item) {
        return new InventoryReservedItem(item.productId(), item.sku(), item.quantity());
    }
}
