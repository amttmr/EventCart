package com.eventcart.cart.mapper;

import com.eventcart.cart.domain.CartDocument;
import com.eventcart.cart.domain.CartItemDocument;
import com.eventcart.cart.dto.AddCartItemRequest;
import com.eventcart.cart.dto.CartItemResponse;
import com.eventcart.cart.dto.CartResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps between cart DTOs and MongoDB cart documents.
 */
@Component
public class CartMapper {
    /**
     * Converts an add-item request into an embedded cart item document.
     *
     * @param request validated add-cart-item request
     * @return embedded cart item document
     */
    public CartItemDocument toItemDocument(AddCartItemRequest request) {
        CartItemDocument item = new CartItemDocument();
        item.setProductId(request.productId());
        item.setSku(request.sku());
        item.setProductName(request.productName());
        item.setUnitPrice(request.unitPrice());
        item.setCurrency(request.currency().toUpperCase());
        item.setQuantity(request.quantity());
        return item;
    }

    /**
     * Converts a cart document into a public cart response.
     *
     * @param cart persisted cart document
     * @return cart response returned by REST APIs
     */
    public CartResponse toResponse(CartDocument cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getCustomerId(),
                items,
                totalItems(cart),
                subtotal(cart),
                currency(cart),
                cart.getVersion(),
                cart.getUpdatedAt()
        );
    }

    /**
     * Converts one embedded cart item into a public cart item response.
     *
     * @param item embedded cart item document
     * @return cart item response
     */
    public CartItemResponse toItemResponse(CartItemDocument item) {
        return new CartItemResponse(
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getCurrency(),
                item.getQuantity(),
                lineTotal(item)
        );
    }

    /**
     * Calculates the total item quantity in a cart.
     *
     * @param cart cart document
     * @return total item quantity
     */
    private int totalItems(CartDocument cart) {
        return cart.getItems()
                .stream()
                .mapToInt(CartItemDocument::getQuantity)
                .sum();
    }

    /**
     * Calculates the subtotal for all cart items.
     *
     * @param cart cart document
     * @return subtotal across all line totals
     */
    private BigDecimal subtotal(CartDocument cart) {
        return cart.getItems()
                .stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Determines the cart currency from its first item.
     *
     * @param cart cart document
     * @return currency code, or {@code INR} for an empty cart
     */
    private String currency(CartDocument cart) {
        return cart.getItems()
                .stream()
                .findFirst()
                .map(CartItemDocument::getCurrency)
                .orElse("INR");
    }

    /**
     * Calculates the line total for one item.
     *
     * @param item embedded cart item document
     * @return unit price multiplied by quantity
     */
    private BigDecimal lineTotal(CartItemDocument item) {
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}

