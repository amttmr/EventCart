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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that owns order business operations.
 */
@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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
        log.info("Placing order customerId={}", request.customerId());
        CartResponse cart = cartClient.getCart(request.customerId());
        log.debug("Cart fetched for order customerId={} cartId={} itemCount={} subtotal={}",
                request.customerId(), cart.cartId(), cart.items().size(), cart.subtotal());
        if (cart.items().isEmpty()) {
            log.warn("Order placement rejected because cart is empty customerId={}", request.customerId());
            throw new EmptyCartException("Cannot place order because cart is empty for customer: " + request.customerId());
        }

        OrderDocument order = orderMapper.toDocument(cart);
        OrderDocument savedOrder = orderRepository.save(order);
        log.info("Order saved orderId={} customerId={} itemCount={} totalAmount={}",
                savedOrder.getId(), savedOrder.getCustomerId(), savedOrder.getItems().size(), savedOrder.getTotalAmount());
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
        log.debug("Fetching order orderId={}", orderId);
        return orderMapper.toResponse(findOrder(orderId));
    }

    /**
     * Retrieves orders for one customer.
     *
     * @param customerId customer ID
     * @return customer orders
     */
    public List<OrderResponse> getOrdersForCustomer(String customerId) {
        log.debug("Fetching customer orders customerId={}", customerId);
        List<OrderDocument> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        log.debug("Customer orders fetched customerId={} count={}", customerId, orders.size());
        return orderMapper.toResponses(orders);
    }

    /**
     * Loads an order or throws a not-found exception.
     *
     * @param orderId order ID
     * @return order document
     */
    private OrderDocument findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found orderId={}", orderId);
                    return new OrderNotFoundException("Order not found: " + orderId);
                });
    }
}
