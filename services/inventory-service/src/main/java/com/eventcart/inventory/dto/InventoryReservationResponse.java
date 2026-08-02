package com.eventcart.inventory.dto;

import com.eventcart.inventory.domain.InventoryReservationStatus;

import java.time.Instant;
import java.util.List;

/**
 * Public representation of an inventory reservation result.
 *
 * @param reservationId MongoDB-generated reservation ID
 * @param orderId order ID this reservation belongs to
 * @param customerId customer that placed the order
 * @param status reservation result status
 * @param items reserved item quantities
 * @param failureReason failure reason for failed reservations
 * @param version optimistic locking version maintained by MongoDB/Spring Data
 * @param createdAt time when the reservation was created
 * @param updatedAt time when the reservation was last modified
 */
public record InventoryReservationResponse(
        String reservationId,
        String orderId,
        String customerId,
        InventoryReservationStatus status,
        List<InventoryReservationItemResponse> items,
        String failureReason,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
