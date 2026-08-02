package com.eventcart.inventory.controller;

import com.eventcart.common.web.ApiResponse;
import com.eventcart.inventory.dto.InventoryItemResponse;
import com.eventcart.inventory.dto.InventoryReservationResponse;
import com.eventcart.inventory.dto.UpsertInventoryItemRequest;
import com.eventcart.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
            @Parameter(description = "Product ID", example = "6a6f2ff6c33ef72269887fec") @PathVariable String productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Inventory stock to create or replace for one product",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Seed enough stock",
                                            summary = "Happy path for reservation",
                                            value = """
                                                    {
                                                      "sku": "SKU-1001",
                                                      "productName": "Mechanical Keyboard",
                                                      "availableQuantity": 25
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Out of stock",
                                            summary = "Use this to test InventoryReservationFailed",
                                            value = """
                                                    {
                                                      "sku": "SKU-1001",
                                                      "productName": "Mechanical Keyboard",
                                                      "availableQuantity": 0
                                                    }
                                                    """
                                    )
                            }
                    )
            )
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
            @Parameter(description = "Product ID", example = "6a6f2ff6c33ef72269887fec") @PathVariable String productId
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
            @Parameter(description = "Order ID", example = "66b1f9f48f8c1c4df8f8a001") @PathVariable String orderId
    ) {
        return ApiResponse.success(inventoryService.getReservation(orderId));
    }
}
