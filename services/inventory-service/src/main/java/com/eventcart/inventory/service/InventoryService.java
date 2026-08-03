package com.eventcart.inventory.service;

import com.eventcart.common.events.OrderCreatedEvent;
import com.eventcart.common.events.OrderCreatedItem;
import com.eventcart.common.events.PaymentFailedEvent;
import com.eventcart.inventory.domain.InventoryItemDocument;
import com.eventcart.inventory.domain.InventoryReservationDocument;
import com.eventcart.inventory.domain.InventoryReservationItemDocument;
import com.eventcart.inventory.domain.InventoryReservationStatus;
import com.eventcart.inventory.dto.InventoryItemResponse;
import com.eventcart.inventory.dto.InventoryReservationResponse;
import com.eventcart.inventory.dto.UpsertInventoryItemRequest;
import com.eventcart.inventory.exception.InventoryItemNotFoundException;
import com.eventcart.inventory.exception.InventoryReservationNotFoundException;
import com.eventcart.inventory.mapper.InventoryMapper;
import com.eventcart.inventory.outbox.InventoryOutboxService;
import com.eventcart.inventory.repository.InventoryItemRepository;
import com.eventcart.inventory.repository.InventoryReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Application service that owns inventory stock and reservation operations.
 */
@Service
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryOutboxService outboxService;

    /**
     * Creates an inventory service.
     *
     * @param inventoryItemRepository repository for stock documents
     * @param reservationRepository repository for reservation results
     * @param inventoryMapper mapper between documents, DTOs, and events
     * @param outboxService outbox service for reliable reservation result publishing
     */
    public InventoryService(
            InventoryItemRepository inventoryItemRepository,
            InventoryReservationRepository reservationRepository,
            InventoryMapper inventoryMapper,
            InventoryOutboxService outboxService
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryMapper = inventoryMapper;
        this.outboxService = outboxService;
    }

    /**
     * Creates or updates available stock for a product.
     *
     * @param productId product ID
     * @param request validated upsert request
     * @return saved inventory item response
     */
    public InventoryItemResponse upsertItem(String productId, UpsertInventoryItemRequest request) {
        log.info("Upserting inventory item productId={} sku={} availableQuantity={}",
                productId, request.sku(), request.availableQuantity());
        InventoryItemDocument item = inventoryItemRepository.findById(productId)
                .orElseGet(InventoryItemDocument::new);
        inventoryMapper.updateItemDocument(productId, request, item);
        InventoryItemDocument savedItem = inventoryItemRepository.save(item);
        log.info("Inventory item saved productId={} availableQuantity={} reservedQuantity={}",
                savedItem.getProductId(), savedItem.getAvailableQuantity(), savedItem.getReservedQuantity());
        return inventoryMapper.toItemResponse(savedItem);
    }

    /**
     * Retrieves one inventory item.
     *
     * @param productId product ID
     * @return inventory item response
     */
    public InventoryItemResponse getItem(String productId) {
        log.debug("Fetching inventory item productId={}", productId);
        return inventoryMapper.toItemResponse(findItem(productId));
    }

    /**
     * Retrieves one reservation result by order ID.
     *
     * @param orderId order ID
     * @return reservation response
     */
    public InventoryReservationResponse getReservation(String orderId) {
        log.debug("Fetching inventory reservation orderId={}", orderId);
        return inventoryMapper.toReservationResponse(reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Inventory reservation not found orderId={}", orderId);
                    return new InventoryReservationNotFoundException("Reservation not found for order: " + orderId);
                }));
    }

    /**
     * Reserves inventory in response to an order-created event.
     *
     * @param event order-created event consumed from Kafka
     * @return reservation result response
     */
    public InventoryReservationResponse reserveInventory(OrderCreatedEvent event) {
        log.info("Reserving inventory orderId={} customerId={} itemCount={}",
                event.orderId(), event.customerId(), event.items().size());
        Optional<InventoryReservationDocument> existingReservation = reservationRepository.findByOrderId(event.orderId());
        if (existingReservation.isPresent()) {
            log.info("Skipping duplicate inventory reservation orderId={} reservationId={} status={}",
                    event.orderId(), existingReservation.get().getId(), existingReservation.get().getStatus());
            return inventoryMapper.toReservationResponse(existingReservation.get());
        }

        Optional<String> failureReason = validateStock(event.items());
        if (failureReason.isPresent()) {
            InventoryReservationDocument failedReservation = saveFailedReservation(event, failureReason.get());
            log.warn("Inventory reservation failed orderId={} reservationId={} reason={}",
                    event.orderId(), failedReservation.getId(), failureReason.get());
            outboxService.enqueueInventoryFailed(inventoryMapper.toInventoryReservationFailedEvent(failedReservation));
            return inventoryMapper.toReservationResponse(failedReservation);
        }

        List<InventoryReservationItemDocument> reservedItems = reserveItems(event.items());
        InventoryReservationDocument reservation = new InventoryReservationDocument();
        reservation.setOrderId(event.orderId());
        reservation.setCustomerId(event.customerId());
        reservation.setStatus(InventoryReservationStatus.RESERVED);
        reservation.setItems(reservedItems);
        reservation.setTotalAmount(event.totalAmount());
        reservation.setCurrency(event.currency());

        InventoryReservationDocument savedReservation = reservationRepository.save(reservation);
        log.info("Inventory reserved orderId={} reservationId={} itemCount={}",
                event.orderId(), savedReservation.getId(), savedReservation.getItems().size());
        outboxService.enqueueInventoryReserved(inventoryMapper.toInventoryReservedEvent(savedReservation));
        return inventoryMapper.toReservationResponse(savedReservation);
    }

    /**
     * Releases previously reserved stock when payment fails for an order.
     *
     * @param event payment-failed event consumed from Kafka
     * @return reservation response when a reservation exists for the order
     */
    public Optional<InventoryReservationResponse> releaseReservationAfterPaymentFailure(PaymentFailedEvent event) {
        log.info("Releasing inventory after payment failure orderId={} paymentId={} reason={}",
                event.orderId(), event.paymentId(), event.reason());
        Optional<InventoryReservationDocument> reservationResult = reservationRepository.findByOrderId(event.orderId());
        if (reservationResult.isEmpty()) {
            log.warn("Cannot release inventory because reservation was not found orderId={} paymentId={}",
                    event.orderId(), event.paymentId());
            return Optional.empty();
        }

        InventoryReservationDocument reservation = reservationResult.get();
        if (reservation.getStatus() == InventoryReservationStatus.RELEASED) {
            log.info("Skipping duplicate inventory release orderId={} reservationId={}",
                    event.orderId(), reservation.getId());
            return Optional.of(inventoryMapper.toReservationResponse(reservation));
        }
        if (reservation.getStatus() == InventoryReservationStatus.FAILED) {
            log.info("Skipping inventory release because reservation already failed orderId={} reservationId={}",
                    event.orderId(), reservation.getId());
            return Optional.of(inventoryMapper.toReservationResponse(reservation));
        }

        releaseItems(reservation.getItems());
        reservation.setStatus(InventoryReservationStatus.RELEASED);
        reservation.setFailureReason("Released after payment failure: " + event.reason());
        InventoryReservationDocument savedReservation = reservationRepository.save(reservation);
        log.info("Inventory released after payment failure orderId={} reservationId={} itemCount={}",
                event.orderId(), savedReservation.getId(), savedReservation.getItems().size());
        return Optional.of(inventoryMapper.toReservationResponse(savedReservation));
    }

    /**
     * Finds one inventory item or throws a not-found exception.
     *
     * @param productId product ID
     * @return inventory item document
     */
    private InventoryItemDocument findItem(String productId) {
        return inventoryItemRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Inventory item not found productId={}", productId);
                    return new InventoryItemNotFoundException("Inventory item not found: " + productId);
                });
    }

    /**
     * Validates that every ordered item has enough available stock.
     *
     * @param items ordered items from the event
     * @return optional failure reason
     */
    private Optional<String> validateStock(List<OrderCreatedItem> items) {
        for (OrderCreatedItem item : items) {
            if (item.quantity() <= 0) {
                log.warn("Invalid reservation quantity productId={} quantity={}", item.productId(), item.quantity());
                return Optional.of("Invalid reservation quantity for product: " + item.productId());
            }

            Optional<InventoryItemDocument> stock = inventoryItemRepository.findById(item.productId());
            if (stock.isEmpty()) {
                log.warn("Reservation stock check failed because product has no stock document productId={}", item.productId());
                return Optional.of("No inventory stock found for product: " + item.productId());
            }

            if (stock.get().getAvailableQuantity() < item.quantity()) {
                log.warn("Reservation stock check failed productId={} availableQuantity={} requestedQuantity={}",
                        item.productId(), stock.get().getAvailableQuantity(), item.quantity());
                return Optional.of("Insufficient stock for product: " + item.productId());
            }
        }
        return Optional.empty();
    }

    /**
     * Applies stock changes after validation has passed.
     *
     * @param items ordered items from the event
     * @return reserved item documents
     */
    private List<InventoryReservationItemDocument> reserveItems(List<OrderCreatedItem> items) {
        List<InventoryReservationItemDocument> reservedItems = new ArrayList<>();

        for (OrderCreatedItem item : items) {
            InventoryItemDocument stock = findItem(item.productId());
            stock.setAvailableQuantity(stock.getAvailableQuantity() - item.quantity());
            stock.setReservedQuantity(stock.getReservedQuantity() + item.quantity());
            inventoryItemRepository.save(stock);
            log.debug("Reserved stock productId={} quantity={} availableQuantity={} reservedQuantity={}",
                    item.productId(), item.quantity(), stock.getAvailableQuantity(), stock.getReservedQuantity());
            reservedItems.add(inventoryMapper.toReservationItem(item));
        }

        return reservedItems;
    }

    /**
     * Releases reserved item quantities back into available stock.
     *
     * @param items reserved item quantities to release
     */
    private void releaseItems(List<InventoryReservationItemDocument> items) {
        for (InventoryReservationItemDocument item : items) {
            Optional<InventoryItemDocument> stockResult = inventoryItemRepository.findById(item.productId());
            if (stockResult.isEmpty()) {
                log.warn("Reserved stock document missing during release productId={} quantity={}",
                        item.productId(), item.quantity());
                continue;
            }

            InventoryItemDocument stock = stockResult.get();
            if (stock.getReservedQuantity() < item.quantity()) {
                log.warn("Reserved quantity lower than release quantity productId={} reservedQuantity={} releaseQuantity={}",
                        item.productId(), stock.getReservedQuantity(), item.quantity());
            }
            stock.setAvailableQuantity(stock.getAvailableQuantity() + item.quantity());
            stock.setReservedQuantity(Math.max(0, stock.getReservedQuantity() - item.quantity()));
            inventoryItemRepository.save(stock);
            log.debug("Released stock productId={} quantity={} availableQuantity={} reservedQuantity={}",
                    item.productId(), item.quantity(), stock.getAvailableQuantity(), stock.getReservedQuantity());
        }
    }

    /**
     * Stores a failed reservation result.
     *
     * @param event order-created event
     * @param reason failure reason
     * @return saved failed reservation document
     */
    private InventoryReservationDocument saveFailedReservation(OrderCreatedEvent event, String reason) {
        InventoryReservationDocument reservation = new InventoryReservationDocument();
        reservation.setOrderId(event.orderId());
        reservation.setCustomerId(event.customerId());
        reservation.setStatus(InventoryReservationStatus.FAILED);
        reservation.setTotalAmount(event.totalAmount());
        reservation.setCurrency(event.currency());
        reservation.setFailureReason(reason);
        return reservationRepository.save(reservation);
    }
}
