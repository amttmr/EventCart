package com.eventcart.cart.controller;

import com.eventcart.cart.dto.AddCartItemRequest;
import com.eventcart.cart.dto.CartResponse;
import com.eventcart.cart.dto.UpdateCartItemQuantityRequest;
import com.eventcart.cart.service.CartService;
import com.eventcart.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes customer cart APIs.
 */
@Tag(name = "Carts", description = "Customer shopping cart APIs")
@RestController
@RequestMapping("/api/v1/carts")
public class CartController {
    private final CartService cartService;

    /**
     * Creates a cart controller.
     *
     * @param cartService cart business service
     */
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Retrieves the active cart for a customer.
     *
     * @param customerId customer ID
     * @return customer cart response
     */
    @Operation(summary = "Get customer cart", description = "Returns the active cart for a customer, creating an empty cart if one does not exist.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart returned")
    @GetMapping("/{customerId}")
    public ApiResponse<CartResponse> getCart(
            @Parameter(description = "Customer ID") @PathVariable String customerId
    ) {
        return ApiResponse.success(cartService.getCart(customerId));
    }

    /**
     * Adds a product snapshot to a customer's cart.
     *
     * @param customerId customer ID
     * @param request validated add-cart-item request
     * @return updated cart response
     */
    @Operation(summary = "Add item to cart", description = "Adds a product snapshot to the customer cart or increases quantity when the product already exists.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/{customerId}/items")
    public ApiResponse<CartResponse> addItem(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ApiResponse.success(cartService.addItem(customerId, request), "Item added to cart");
    }

    /**
     * Updates the quantity for one cart item.
     *
     * @param customerId customer ID
     * @param productId product ID
     * @param request validated quantity update request
     * @return updated cart response
     */
    @Operation(summary = "Update cart item quantity", description = "Updates the quantity of an existing product in the customer cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quantity updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/{customerId}/items/{productId}")
    public ApiResponse<CartResponse> updateItemQuantity(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        return ApiResponse.success(
                cartService.updateItemQuantity(customerId, productId, request),
                "Cart item quantity updated"
        );
    }

    /**
     * Removes one product from a customer's cart.
     *
     * @param customerId customer ID
     * @param productId product ID
     * @return updated cart response
     */
    @Operation(summary = "Remove cart item", description = "Removes one product from the customer cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/{customerId}/items/{productId}")
    public ApiResponse<CartResponse> removeItem(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Parameter(description = "Product ID") @PathVariable String productId
    ) {
        return ApiResponse.success(cartService.removeItem(customerId, productId), "Cart item removed");
    }

    /**
     * Clears all items from a customer's cart.
     *
     * @param customerId customer ID
     * @return HTTP 204 response when the cart is cleared
     */
    @Operation(summary = "Clear cart", description = "Removes all items from the customer cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Cart cleared")
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(
            @Parameter(description = "Customer ID") @PathVariable String customerId
    ) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}

