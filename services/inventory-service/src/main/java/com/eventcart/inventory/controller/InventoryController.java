package com.eventcart.inventory.controller;

import com.eventcart.common.web.ApiResponse;
import com.eventcart.inventory.dto.InventoryItemResponse;
import com.eventcart.inventory.dto.InventoryReservationResponse;
import com.eventcart.inventory.dto.UpsertInventoryItemRequest;
import com.eventcart.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes inventory stock and reservation APIs.
 */
@Tag(name = "Inventory", description = "Inventory stock and reservation APIs")
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    /**
     * Creates an inventory controller.
     *
     * @param inventoryService inventory business service
     */
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Creates or updates inventory stock for one product.
     *
     * @param productId product ID
     * @param request validated upsert request
     * @return saved inventory item response
     */
    @Operation(summary = "Upsert inventory item", description = "Creates or updates available stock for one product.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inventory item saved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PutMapping("/{productId}")
    public ApiResponse<InventoryItemResponse> upsertItem(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Valid @RequestBody UpsertInventoryItemRequest request
    ) {
        return ApiResponse.success(inventoryService.upsertItem(productId, request), "Inventory item saved");
    }

    /**
     * Retrieves one inventory item.
     *
     * @param productId product ID
     * @return inventory item response
     */
    @Operation(summary = "Get inventory item", description = "Returns stock information for one product.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inventory item returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Inventory item not found")
    })
    @GetMapping("/{productId}")
    public ApiResponse<InventoryItemResponse> getItem(
            @Parameter(description = "Product ID") @PathVariable String productId
    ) {
        return ApiResponse.success(inventoryService.getItem(productId));
    }

    /**
     * Retrieves the reservation result for one order.
     *
     * @param orderId order ID
     * @return reservation response
     */
    @Operation(summary = "Get reservation result", description = "Returns the inventory reservation result for one order.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reservation returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/reservations/{orderId}")
    public ApiResponse<InventoryReservationResponse> getReservation(
            @Parameter(description = "Order ID") @PathVariable String orderId
    ) {
        return ApiResponse.success(inventoryService.getReservation(orderId));
    }
}
