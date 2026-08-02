package com.eventcart.inventory.service;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.events.OrderCreatedItem;
import com.eventcart.inventory.domain.InventoryItemDocument;
import com.eventcart.inventory.domain.InventoryReservationDocument;
import com.eventcart.inventory.domain.InventoryReservationStatus;
import com.eventcart.inventory.dto.InventoryReservationResponse;
import com.eventcart.inventory.event.InventoryEventPublisher;
import com.eventcart.inventory.mapper.InventoryMapper;
import com.eventcart.inventory.repository.InventoryItemRepository;
import com.eventcart.inventory.repository.InventoryReservationRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link InventoryService}.
 */
class InventoryServiceTest {
    private final InventoryItemRepository inventoryItemRepository = mock(InventoryItemRepository.class);
    private final InventoryReservationRepository reservationRepository = mock(InventoryReservationRepository.class);
    private final InventoryMapper inventoryMapper = new InventoryMapper();
    private final InventoryEventPublisher eventPublisher = mock(InventoryEventPublisher.class);
    private final InventoryService inventoryService = new InventoryService(
            inventoryItemRepository,
            reservationRepository,
            inventoryMapper,
            eventPublisher
    );

    /**
     * Verifies that inventory is reserved and a success event is published when stock is sufficient.
     */
    @Test
    void reserveInventoryShouldReserveStockAndPublishSuccess() {
        InventoryItemDocument stock = stock(5);
        when(reservationRepository.findByOrderId("order-1")).thenReturn(Optional.empty());
        when(inventoryItemRepository.findById("product-1")).thenReturn(Optional.of(stock));
        when(inventoryItemRepository.save(stock)).thenReturn(stock);
        when(reservationRepository.save(any(InventoryReservationDocument.class))).thenAnswer(invocation -> {
            InventoryReservationDocument reservation = invocation.getArgument(0);
            reservation.setId("reservation-1");
            return reservation;
        });

        InventoryReservationResponse response = inventoryService.reserveInventory(orderCreatedEvent(2));

        assertThat(response.status()).isEqualTo(InventoryReservationStatus.RESERVED);
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalAmount()).isEqualByComparingTo("13998.00");
        assertThat(response.currency()).isEqualTo("INR");
        assertThat(stock.getAvailableQuantity()).isEqualTo(3);
        assertThat(stock.getReservedQuantity()).isEqualTo(2);
        verify(eventPublisher).publishInventoryReserved(any());
        verify(eventPublisher, never()).publishInventoryFailed(any());
    }

    /**
     * Verifies that inventory reservation fails and stock is not changed when quantity is insufficient.
     */
    @Test
    void reserveInventoryShouldFailWhenStockIsInsufficient() {
        InventoryItemDocument stock = stock(1);
        when(reservationRepository.findByOrderId("order-1")).thenReturn(Optional.empty());
        when(inventoryItemRepository.findById("product-1")).thenReturn(Optional.of(stock));
        when(reservationRepository.save(any(InventoryReservationDocument.class))).thenAnswer(invocation -> {
            InventoryReservationDocument reservation = invocation.getArgument(0);
            reservation.setId("reservation-1");
            return reservation;
        });

        InventoryReservationResponse response = inventoryService.reserveInventory(orderCreatedEvent(2));

        assertThat(response.status()).isEqualTo(InventoryReservationStatus.FAILED);
        assertThat(response.failureReason()).contains("Insufficient stock");
        assertThat(stock.getAvailableQuantity()).isEqualTo(1);
        assertThat(stock.getReservedQuantity()).isZero();
        verify(inventoryItemRepository, never()).save(any());
        verify(eventPublisher).publishInventoryFailed(any());
        verify(eventPublisher, never()).publishInventoryReserved(any());
    }

    /**
     * Creates an inventory stock document for service tests.
     *
     * @param availableQuantity available quantity
     * @return inventory stock document
     */
    private InventoryItemDocument stock(int availableQuantity) {
        InventoryItemDocument stock = new InventoryItemDocument();
        stock.setProductId("product-1");
        stock.setSku("SKU-1");
        stock.setProductName("Mechanical Keyboard");
        stock.setAvailableQuantity(availableQuantity);
        stock.setReservedQuantity(0);
        return stock;
    }

    /**
     * Creates an order-created event for service tests.
     *
     * @param quantity ordered quantity
     * @return order-created event
     */
    private OrderCreatedEvent orderCreatedEvent(int quantity) {
        return new OrderCreatedEvent(
                EventMetadata.create(OrderCreatedEvent.EVENT_TYPE, OrderCreatedEvent.VERSION, "order-1"),
                "order-1",
                "customer-1",
                List.of(new OrderCreatedItem(
                        "product-1",
                        "SKU-1",
                        "Mechanical Keyboard",
                        new BigDecimal("6999.00"),
                        "INR",
                        quantity,
                        new BigDecimal("13998.00")
                )),
                new BigDecimal("13998.00"),
                "INR"
        );
    }
}
