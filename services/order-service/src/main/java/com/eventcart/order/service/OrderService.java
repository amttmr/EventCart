package com.eventcart.order.service;

import com.eventcart.common.events.InventoryReservationFailedEvent;
import com.eventcart.common.events.InventoryReservedEvent;
import com.eventcart.common.events.PaymentCompletedEvent;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.common.security.CustomerAccessPolicy;
import com.eventcart.order.client.CartClient;
import com.eventcart.order.client.CartResponse;
import com.eventcart.order.domain.OrderDocument;
import com.eventcart.order.domain.OrderStatus;
import com.eventcart.order.dto.OrderResponse;
import com.eventcart.order.dto.PlaceOrderRequest;
import com.eventcart.order.exception.CartServiceUnavailableException;
import com.eventcart.order.exception.EmptyCartException;
import com.eventcart.order.exception.OrderNotFoundException;
import com.eventcart.order.mapper.OrderMapper;
import com.eventcart.order.outbox.OrderOutboxService;
import com.eventcart.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that owns order business operations.
 */
@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final OrderMapper orderMapper;
    private final OrderOutboxService orderOutboxService;
    private final OrderIdempotencyService orderIdempotencyService;
    private final CustomerAccessPolicy customerAccessPolicy;

    /**
     * Creates an order service.
     *
     * @param orderRepository repository for order persistence
     * @param cartClient HTTP client for cart-service
     * @param orderMapper mapper between cart data, order documents, DTOs, and events
     * @param orderOutboxService outbox service used to enqueue order events
     * @param orderIdempotencyService Redis-backed idempotency helper
     * @param customerAccessPolicy ownership policy for customer-scoped lookups
     */
    public OrderService(
            OrderRepository orderRepository,
            CartClient cartClient,
            OrderMapper orderMapper,
            OrderOutboxService orderOutboxService,
            OrderIdempotencyService orderIdempotencyService,
            CustomerAccessPolicy customerAccessPolicy
    ) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.orderMapper = orderMapper;
        this.orderOutboxService = orderOutboxService;
        this.orderIdempotencyService = orderIdempotencyService;
        this.customerAccessPolicy = customerAccessPolicy;
    }

    /**
     * Places an order from the customer's current cart.
     *
     * @param request validated place-order request
     * @return created order response
     */
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        log.info("Placing order customerId={} idempotencyKeyPresent={}",
                request.customerId(), request.idempotencyKey() != null && !request.idempotencyKey().isBlank());
        Optional<String> existingOrderId = orderIdempotencyService.begin(request.idempotencyKey());
        if (existingOrderId.isPresent()) {
            log.info("Returning existing order for idempotent request customerId={} orderId={}",
                    request.customerId(), existingOrderId.get());
            return getOrder(existingOrderId.get());
        }

        try {
            CartResponse cart = cartClient.getCart(request.customerId());
            log.debug("Cart fetched for order customerId={} cartId={} itemCount={} subtotal={}",
                    request.customerId(), cart.cartId(), cart.items().size(), cart.subtotal());
            if (cart.items().isEmpty()) {
                log.warn("Order placement rejected because cart is empty customerId={}", request.customerId());
                throw new EmptyCartException("Cannot place order because cart is empty for customer: " + request.customerId());
            }

            OrderDocument order = orderMapper.toDocument(cart);
            OrderDocument savedOrder = orderRepository.save(order);
            orderIdempotencyService.complete(request.idempotencyKey(), savedOrder.getId());
            log.info("Order saved orderId={} customerId={} itemCount={} totalAmount={}",
                    savedOrder.getId(), savedOrder.getCustomerId(), savedOrder.getItems().size(), savedOrder.getTotalAmount());
            orderOutboxService.enqueueOrderCreated(orderMapper.toOrderCreatedEvent(savedOrder));
            return orderMapper.toResponse(savedOrder);
        } catch (RuntimeException ex) {
            orderIdempotencyService.clearIfInProgress(request.idempotencyKey());
            throw ex;
        }
    }

    /**
     * Applies a successful inventory reservation event to an order.
     *
     * @param event Kafka event published by inventory-service
     */
    public void markInventoryReserved(InventoryReservedEvent event) {
        log.info("Applying inventory reservation success orderId={} customerId={} itemCount={}",
                event.orderId(), event.customerId(), event.items().size());
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {
                log.info("Ignoring duplicate inventory reserved event orderId={}", event.orderId());
                clearCartAfterReservation(event.customerId(), event.orderId());
                return;
            }
            if (order.getStatus() == OrderStatus.INVENTORY_FAILED) {
                log.warn("Ignoring inventory reserved event because order is already failed orderId={}", event.orderId());
                return;
            }
            if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED || order.getStatus() == OrderStatus.PAYMENT_FAILED) {
                log.info("Inventory reserved event arrived after payment result; keeping final status orderId={} status={}",
                        event.orderId(), order.getStatus());
                clearCartAfterReservation(event.customerId(), event.orderId());
                return;
            }

            order.setStatus(OrderStatus.INVENTORY_RESERVED);
            order.setStatusReason(null);
            orderRepository.save(order);
            log.info("Order status updated after inventory reservation orderId={} status={}",
                    event.orderId(), OrderStatus.INVENTORY_RESERVED);
            clearCartAfterReservation(event.customerId(), event.orderId());
        }, () -> log.warn("Inventory reserved event ignored because order was not found orderId={}", event.orderId()));
    }

    /**
     * Applies a failed inventory reservation event to an order.
     *
     * @param event Kafka event published by inventory-service
     */
    public void markInventoryFailed(InventoryReservationFailedEvent event) {
        log.info("Applying inventory reservation failure orderId={} customerId={} reason={}",
                event.orderId(), event.customerId(), event.reason());
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.INVENTORY_FAILED) {
                log.info("Ignoring duplicate inventory failed event orderId={}", event.orderId());
                return;
            }
            if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {
                log.warn("Ignoring inventory failed event because order is already reserved orderId={}", event.orderId());
                return;
            }
            if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED || order.getStatus() == OrderStatus.PAYMENT_FAILED) {
                log.warn("Ignoring inventory failed event because order already has payment result orderId={} status={}",
                        event.orderId(), order.getStatus());
                return;
            }

            order.setStatus(OrderStatus.INVENTORY_FAILED);
            order.setStatusReason(event.reason());
            orderRepository.save(order);
            log.info("Order status updated after inventory failure orderId={} status={} reason={}",
                    event.orderId(), OrderStatus.INVENTORY_FAILED, event.reason());
        }, () -> log.warn("Inventory failed event ignored because order was not found orderId={}", event.orderId()));
    }

    /**
     * Applies a successful payment event to an order.
     *
     * @param event Kafka event published by payment-service
     */
    public void markPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Applying payment completion orderId={} paymentId={} amount={} currency={}",
                event.orderId(), event.paymentId(), event.amount(), event.currency());
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED) {
                log.info("Ignoring duplicate payment completed event orderId={}", event.orderId());
                return;
            }
            if (order.getStatus() == OrderStatus.PAYMENT_FAILED || order.getStatus() == OrderStatus.INVENTORY_FAILED) {
                log.warn("Ignoring payment completed event because order is already in terminal failure state orderId={} status={}",
                        event.orderId(), order.getStatus());
                return;
            }

            order.setStatus(OrderStatus.PAYMENT_COMPLETED);
            order.setStatusReason(null);
            orderRepository.save(order);
            log.info("Order status updated after payment completion orderId={} status={} paymentId={}",
                    event.orderId(), OrderStatus.PAYMENT_COMPLETED, event.paymentId());
        }, () -> log.warn("Payment completed event ignored because order was not found orderId={}", event.orderId()));
    }

    /**
     * Applies a failed payment event to an order.
     *
     * @param event Kafka event published by payment-service
     */
    public void markPaymentFailed(PaymentFailedEvent event) {
        log.info("Applying payment failure orderId={} paymentId={} reason={}",
                event.orderId(), event.paymentId(), event.reason());
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
                log.info("Ignoring duplicate payment failed event orderId={}", event.orderId());
                return;
            }
            if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED || order.getStatus() == OrderStatus.INVENTORY_FAILED) {
                log.warn("Ignoring payment failed event because order is already in terminal state orderId={} status={}",
                        event.orderId(), order.getStatus());
                return;
            }

            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setStatusReason(event.reason());
            orderRepository.save(order);
            log.info("Order status updated after payment failure orderId={} status={} reason={}",
                    event.orderId(), OrderStatus.PAYMENT_FAILED, event.reason());
        }, () -> log.warn("Payment failed event ignored because order was not found orderId={}", event.orderId()));
    }

    /**
     * Retrieves one order by ID.
     *
     * @param orderId order ID
     * @return order response
     */
    public OrderResponse getOrder(String orderId) {
        log.debug("Fetching order orderId={}", orderId);
        OrderDocument order = findOrder(orderId);
        customerAccessPolicy.requireCustomerAccess(
                order.getCustomerId(),
                SecurityContextHolder.getContext().getAuthentication()
        );
        return orderMapper.toResponse(order);
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

    /**
     * Clears the customer's cart after inventory has been reserved.
     *
     * @param customerId customer whose cart should be cleared
     * @param orderId order that triggered the cart cleanup
     */
    private void clearCartAfterReservation(String customerId, String orderId) {
        try {
            cartClient.clearCart(customerId);
        } catch (CartServiceUnavailableException ex) {
            log.warn("Order inventory was reserved but cart cleanup failed orderId={} customerId={}",
                    orderId, customerId, ex);
        }
    }
}
