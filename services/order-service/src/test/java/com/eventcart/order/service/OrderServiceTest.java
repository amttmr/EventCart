package com.eventcart.order.service;

import com.eventcart.common.events.EventMetadata;
import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.common.events.InventoryReservedItem;
import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.order.client.CartClient;
import com.eventcart.order.client.CartItemResponse;
import com.eventcart.order.client.CartResponse;
import com.eventcart.order.domain.OrderDocument;
import com.eventcart.order.domain.OrderStatus;
import com.eventcart.order.dto.OrderResponse;
import com.eventcart.order.dto.PlaceOrderRequest;
import com.eventcart.order.event.OrderEventPublisher;
import com.eventcart.order.mapper.OrderMapper;
import com.eventcart.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OrderService}.
 */
class OrderServiceTest {
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CartClient cartClient = mock(CartClient.class);
    private final OrderMapper orderMapper = new OrderMapper();
    private final OrderEventPublisher orderEventPublisher = mock(OrderEventPublisher.class);
    private final OrderIdempotencyService orderIdempotencyService = mock(OrderIdempotencyService.class);
    private final OrderService orderService = new OrderService(
            orderRepository,
            cartClient,
            orderMapper,
            orderEventPublisher,
            orderIdempotencyService
    );

    /**
     * Verifies that placing an order fetches the cart, saves an order snapshot,
     * and publishes the order-created event.
     */
    @Test
    void placeOrderShouldCreateOrderAndPublishEvent() {
        when(orderIdempotencyService.begin(null)).thenReturn(Optional.empty());
        when(cartClient.getCart("customer-1")).thenReturn(cart());
        when(orderRepository.save(any(OrderDocument.class))).thenAnswer(invocation -> {
            OrderDocument order = invocation.getArgument(0);
            order.setId("order-1");
            return order;
        });

        OrderResponse response = orderService.placeOrder(new PlaceOrderRequest("customer-1"));

        assertThat(response.orderId()).isEqualTo("order-1");
        assertThat(response.customerId()).isEqualTo("customer-1");
        assertThat(response.items()).hasSize(1);
        verify(cartClient).getCart("customer-1");
        verify(orderRepository).save(any(OrderDocument.class));
        verify(orderIdempotencyService).complete(null, "order-1");
        verify(orderEventPublisher).publishOrderCreated(any());
    }

    /**
     * Verifies that a completed idempotency key returns the original order.
     */
    @Test
    void placeOrderShouldReturnExistingOrderForCompletedIdempotencyKey() {
        OrderDocument existingOrder = order();
        when(orderIdempotencyService.begin("customer-1-order-1")).thenReturn(Optional.of("order-1"));
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(existingOrder));

        OrderResponse response = orderService.placeOrder(new PlaceOrderRequest("customer-1", "customer-1-order-1"));

        assertThat(response.orderId()).isEqualTo("order-1");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        verify(cartClient, never()).getCart("customer-1");
        verify(orderRepository, never()).save(any(OrderDocument.class));
        verify(orderEventPublisher, never()).publishOrderCreated(any());
    }

    /**
     * Verifies that successful inventory reservation updates order status and clears the cart.
     */
    @Test
    void markInventoryReservedShouldUpdateStatusAndClearCart() {
        OrderDocument order = order();
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.markInventoryReserved(new InventoryReservedEvent(
                EventMetadata.create(InventoryReservedEvent.EVENT_TYPE, InventoryReservedEvent.VERSION, "order-1"),
                "order-1",
                "customer-1",
                List.of(new InventoryReservedItem("product-1", "SKU-1", 1)),
                new BigDecimal("6999.00"),
                "INR"
        ));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);
        assertThat(order.getStatusReason()).isNull();
        verify(orderRepository).save(order);
        verify(cartClient).clearCart("customer-1");
    }

    /**
     * Verifies that failed inventory reservation updates order status and reason.
     */
    @Test
    void markInventoryFailedShouldUpdateStatusReason() {
        OrderDocument order = order();
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.markInventoryFailed(new InventoryReservationFailedEvent(
                EventMetadata.create(InventoryReservationFailedEvent.EVENT_TYPE, InventoryReservationFailedEvent.VERSION, "order-1"),
                "order-1",
                "customer-1",
                "Insufficient stock for SKU-1"
        ));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.INVENTORY_FAILED);
        assertThat(order.getStatusReason()).isEqualTo("Insufficient stock for SKU-1");
        verify(orderRepository).save(order);
        verify(cartClient, never()).clearCart("customer-1");
    }

    /**
     * Verifies that successful payment updates the order to a final paid status.
     */
    @Test
    void markPaymentCompletedShouldUpdateStatus() {
        OrderDocument order = order();
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.markPaymentCompleted(new PaymentCompletedEvent(
                EventMetadata.create(PaymentCompletedEvent.EVENT_TYPE, PaymentCompletedEvent.VERSION, "order-1"),
                "payment-1",
                "order-1",
                "customer-1",
                new BigDecimal("6999.00"),
                "INR",
                "MockPay-transaction-1"
        ));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        assertThat(order.getStatusReason()).isNull();
        verify(orderRepository).save(order);
    }

    /**
     * Verifies that failed payment updates the order status and reason.
     */
    @Test
    void markPaymentFailedShouldUpdateStatusReason() {
        OrderDocument order = order();
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.markPaymentFailed(new PaymentFailedEvent(
                EventMetadata.create(PaymentFailedEvent.EVENT_TYPE, PaymentFailedEvent.VERSION, "order-1"),
                "payment-1",
                "order-1",
                "customer-1",
                new BigDecimal("6999.00"),
                "INR",
                "Mock payment declined"
        ));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThat(order.getStatusReason()).isEqualTo("Mock payment declined");
        verify(orderRepository).save(order);
    }

    /**
     * Creates a cart response for service tests.
     *
     * @return cart response
     */
    private CartResponse cart() {
        return new CartResponse(
                "cart-1",
                "customer-1",
                List.of(new CartItemResponse(
                        "product-1",
                        "SKU-1",
                        "Mechanical Keyboard",
                        new BigDecimal("6999.00"),
                        "INR",
                        1,
                        new BigDecimal("6999.00")
                )),
                1,
                new BigDecimal("6999.00"),
                "INR",
                0L,
                Instant.now()
        );
    }

    /**
     * Creates an order document for service tests.
     *
     * @return order document
     */
    private OrderDocument order() {
        OrderDocument order = orderMapper.toDocument(cart());
        order.setId("order-1");
        return order;
    }
}
