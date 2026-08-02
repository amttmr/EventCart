package com.eventcart.cart.service;

import com.eventcart.cart.domain.CartDocument;
import com.eventcart.cart.domain.CartItemDocument;
import com.eventcart.cart.dto.AddCartItemRequest;
import com.eventcart.cart.dto.CartResponse;
import com.eventcart.cart.dto.UpdateCartItemQuantityRequest;
import com.eventcart.cart.exception.CartItemNotFoundException;
import com.eventcart.cart.mapper.CartMapper;
import com.eventcart.cart.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Application service that owns cart business operations.
 */
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    /**
     * Creates a cart service.
     *
     * @param cartRepository repository for cart persistence
     * @param cartMapper mapper between DTOs and documents
     */
    public CartService(CartRepository cartRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
    }

    /**
     * Returns a customer's active cart, creating an empty cart if needed.
     *
     * @param customerId customer ID
     * @return cart response
     */
    public CartResponse getCart(String customerId) {
        return cartMapper.toResponse(findOrCreateCart(customerId));
    }

    /**
     * Adds an item to the customer's cart.
     *
     * <p>If the same product already exists in the cart, this method increases
     * the quantity instead of adding a duplicate item row.</p>
     *
     * @param customerId customer ID
     * @param request validated add-cart-item request
     * @return updated cart response
     */
    public CartResponse addItem(String customerId, AddCartItemRequest request) {
        CartDocument cart = findOrCreateCart(customerId);
        Optional<CartItemDocument> existingItem = findItem(cart, request.productId());

        if (existingItem.isPresent()) {
            CartItemDocument item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        } else {
            cart.getItems().add(cartMapper.toItemDocument(request));
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    /**
     * Updates the quantity of an existing cart item.
     *
     * @param customerId customer ID
     * @param productId product ID
     * @param request validated quantity update request
     * @return updated cart response
     */
    public CartResponse updateItemQuantity(
            String customerId,
            String productId,
            UpdateCartItemQuantityRequest request
    ) {
        CartDocument cart = findOrCreateCart(customerId);
        CartItemDocument item = findItem(cart, productId)
                .orElseThrow(() -> new CartItemNotFoundException("Product not found in cart: " + productId));

        item.setQuantity(request.quantity());
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    /**
     * Removes one item from a customer's cart.
     *
     * @param customerId customer ID
     * @param productId product ID
     * @return updated cart response
     */
    public CartResponse removeItem(String customerId, String productId) {
        CartDocument cart = findOrCreateCart(customerId);
        int itemIndex = findItemIndex(cart, productId);
        cart.getItems().remove(itemIndex);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    /**
     * Clears all items from a customer's cart.
     *
     * @param customerId customer ID
     */
    public void clearCart(String customerId) {
        CartDocument cart = findOrCreateCart(customerId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Finds an existing cart or creates and stores a new empty cart.
     *
     * @param customerId customer ID
     * @return persisted cart document
     */
    private CartDocument findOrCreateCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createCart(customerId));
    }

    /**
     * Creates a new empty cart for a customer.
     *
     * @param customerId customer ID
     * @return persisted empty cart
     */
    private CartDocument createCart(String customerId) {
        CartDocument cart = new CartDocument();
        cart.setCustomerId(customerId);
        return cartRepository.save(cart);
    }

    /**
     * Finds a cart item by product ID.
     *
     * @param cart cart document
     * @param productId product ID
     * @return optional matching cart item
     */
    private Optional<CartItemDocument> findItem(CartDocument cart, String productId) {
        return cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    /**
     * Finds the list index of a cart item by product ID.
     *
     * @param cart cart document
     * @param productId product ID
     * @return zero-based item index
     */
    private int findItemIndex(CartDocument cart, String productId) {
        List<CartItemDocument> items = cart.getItems();
        return IntStream.range(0, items.size())
                .filter(index -> items.get(index).getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Product not found in cart: " + productId));
    }
}

