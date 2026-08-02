package com.eventcart.inventory.mapper;

import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.common.events.OrderCreatedItem;
import com.eventcart.inventory.domain.InventoryItemDocument;
import com.eventcart.inventory.domain.InventoryReservationDocument;
import com.eventcart.inventory.domain.InventoryReservationItemDocument;
import com.eventcart.inventory.domain.InventoryReservationStatus;
import com.eventcart.inventory.dto.InventoryItemResponse;
import com.eventcart.inventory.dto.UpsertInventoryItemRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InventoryMapper}.
 */
class InventoryMapperTest {
    private final InventoryMapper inventoryMapper = new InventoryMapper();

    /**
     * Verifies that an upsert request updates the inventory document fields.
     */
    @Test
    void updateItemDocumentShouldApplyRequest() {
        InventoryItemDocument item = new InventoryItemDocument();

        inventoryMapper.updateItemDocument(
                "product-1",
                new UpsertInventoryItemRequest("SKU-1", "Mechanical Keyboard", 10),
                item
        );

        InventoryItemResponse response = inventoryMapper.toItemResponse(item);
        assertThat(response.productId()).isEqualTo("product-1");
        assertThat(response.sku()).isEqualTo("SKU-1");
        assertThat(response.availableQuantity()).isEqualTo(10);
    }

    /**
     * Verifies that a reservation document becomes an inventory-reserved event.
     */
    @Test
    void toInventoryReservedEventShouldMapReservation() {
        InventoryReservationDocument reservation = new InventoryReservationDocument();
        reservation.setOrderId("order-1");
        reservation.setCustomerId("customer-1");
        reservation.setStatus(InventoryReservationStatus.RESERVED);
        reservation.setItems(List.of(new InventoryReservationItemDocument("product-1", "SKU-1", 2)));
        reservation.setTotalAmount(new BigDecimal("13998.00"));
        reservation.setCurrency("INR");

        InventoryReservedEvent event = inventoryMapper.toInventoryReservedEvent(reservation);

        assertThat(event.metadata().eventType()).isEqualTo(InventoryReservedEvent.EVENT_TYPE);
        assertThat(event.orderId()).isEqualTo("order-1");
        assertThat(event.items()).hasSize(1);
        assertThat(event.items().getFirst().quantity()).isEqualTo(2);
        assertThat(event.totalAmount()).isEqualByComparingTo("13998.00");
        assertThat(event.currency()).isEqualTo("INR");
    }

    /**
     * Verifies that an order-created item becomes a reservation item document.
     */
    @Test
    void toReservationItemShouldMapOrderItem() {
        OrderCreatedItem orderItem = new OrderCreatedItem(
                "product-1",
                "SKU-1",
                "Mechanical Keyboard",
                new BigDecimal("6999.00"),
                "INR",
                2,
                new BigDecimal("13998.00")
        );

        InventoryReservationItemDocument reservationItem = inventoryMapper.toReservationItem(orderItem);

        assertThat(reservationItem.productId()).isEqualTo("product-1");
        assertThat(reservationItem.quantity()).isEqualTo(2);
    }
}
