package com.eventcart.order.mapper;

import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.order.client.CartItemResponse;
import com.eventcart.order.client.CartResponse;
import com.eventcart.order.domain.OrderDocument;
import com.eventcart.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OrderMapper}.
 */
class OrderMapperTest {
    private final OrderMapper orderMapper = new OrderMapper();

    /**
     * Verifies that cart data becomes a stable order snapshot.
     */
    @Test
    void toDocumentShouldMapCartSnapshot() {
        CartResponse cart = cart();

        OrderDocument order = orderMapper.toDocument(cart);

        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().getFirst().productName()).isEqualTo("Mechanical Keyboard");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("6999.00");
        assertThat(order.getCurrency()).isEqualTo("INR");
    }

    /**
     * Verifies that an order document becomes an OrderCreated event.
     */
    @Test
    void toOrderCreatedEventShouldMapOrderSnapshot() {
        OrderDocument order = orderMapper.toDocument(cart());
        order.setId("order-1");

        OrderCreatedEvent event = orderMapper.toOrderCreatedEvent(order);

        assertThat(event.metadata().eventType()).isEqualTo(OrderCreatedEvent.EVENT_TYPE);
        assertThat(event.orderId()).isEqualTo("order-1");
        assertThat(event.customerId()).isEqualTo("customer-1");
        assertThat(event.items()).hasSize(1);
        assertThat(event.totalAmount()).isEqualByComparingTo("6999.00");
    }

    /**
     * Verifies that an order document becomes the public API response.
     */
    @Test
    void toResponseShouldMapOrderDocument() {
        OrderDocument order = orderMapper.toDocument(cart());
        order.setId("order-1");

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response.orderId()).isEqualTo("order-1");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().lineTotal()).isEqualByComparingTo("6999.00");
    }

    /**
     * Creates a cart response for mapper tests.
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
