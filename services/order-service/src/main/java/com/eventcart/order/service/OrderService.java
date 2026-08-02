package com.eventcart.order.service;

import com.eventcart.order.client.CartResponse;
import com.eventcart.order.client.CartClient;
import com.eventcart.order.domain.OrderDocument;
import com.eventcart.order.dto.OrderResponse;
import com.eventcart.order.dto.PlaceOrderRequest;
import com.eventcart.order.event.OrderEventPublisher;
import com.eventcart.order.exception.EmptyCartException;
import com.eventcart.order.exception.OrderNotFoundException;
import com.eventcart.order.mapper.OrderMapper;
import com.eventcart.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that owns order business operations.
 */
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * Creates an order service.
     *
     * @param orderRepository repository for order persistence
     * @param cartClient HTTP client for cart-service
     * @param orderMapper mapper between cart data, order documents, DTOs, and events
     * @param orderEventPublisher Kafka publisher for order events
     */
    public OrderService(
            OrderRepository orderRepository,
            CartClient cartClient,
            OrderMapper orderMapper,
            OrderEventPublisher orderEventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.orderMapper = orderMapper;
        this.orderEventPublisher = orderEventPublisher;
    }

    /**
     * Places an order from the customer's current cart.
     *
     * @param request validated place-order request
     * @return created order response
     */
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        CartResponse cart = cartClient.getCart(request.customerId());
        if (cart.items().isEmpty()) {
            throw new EmptyCartException("Cannot place order because cart is empty for customer: " + request.customerId());
        }

        OrderDocument order = orderMapper.toDocument(cart);
        OrderDocument savedOrder = orderRepository.save(order);
        orderEventPublisher.publishOrderCreated(orderMapper.toOrderCreatedEvent(savedOrder));
        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Retrieves one order by ID.
     *
     * @param orderId order ID
     * @return order response
     */
    public OrderResponse getOrder(String orderId) {
        return orderMapper.toResponse(findOrder(orderId));
    }

    /**
     * Retrieves orders for one customer.
     *
     * @param customerId customer ID
     * @return customer orders
     */
    public List<OrderResponse> getOrdersForCustomer(String customerId) {
        return orderMapper.toResponses(orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    /**
     * Loads an order or throws a not-found exception.
     *
     * @param orderId order ID
     * @return order document
     */
    private OrderDocument findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }
}
