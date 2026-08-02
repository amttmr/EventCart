package com.eventcart.order.service;

import com.eventcart.order.client.CartClient;
import com.eventcart.order.client.CartItemResponse;
import com.eventcart.order.client.CartResponse;
import com.eventcart.order.domain.OrderDocument;
import com.eventcart.order.dto.OrderResponse;
import com.eventcart.order.dto.PlaceOrderRequest;
import com.eventcart.order.event.OrderEventPublisher;
import com.eventcart.order.mapper.OrderMapper;
import com.eventcart.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    private final OrderService orderService = new OrderService(orderRepository, cartClient, orderMapper, orderEventPublisher);

    /**
     * Verifies that placing an order fetches the cart, saves an order snapshot,
     * and publishes the order-created event.
     */
    @Test
    void placeOrderShouldCreateOrderAndPublishEvent() {
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
        verify(orderEventPublisher).publishOrderCreated(any());
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
}
