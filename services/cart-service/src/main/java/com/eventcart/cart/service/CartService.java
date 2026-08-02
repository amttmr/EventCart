package com.eventcart.cart.service;

import com.eventcart.cart.client.CatalogClient;
import com.eventcart.cart.client.CatalogProductResponse;
import com.eventcart.cart.domain.CartDocument;
import com.eventcart.cart.domain.CartItemDocument;
import com.eventcart.cart.dto.AddCartItemRequest;
import com.eventcart.cart.dto.CartResponse;
import com.eventcart.cart.dto.UpdateCartItemQuantityRequest;
import com.eventcart.cart.exception.CartItemNotFoundException;
import com.eventcart.cart.mapper.CartMapper;
import com.eventcart.cart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Application service that owns cart business operations.
 */
@Service
public class CartService {
    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CatalogClient catalogClient;

    /**
     * Creates a cart service.
     *
     * @param cartRepository repository for cart persistence
     * @param cartMapper mapper between DTOs and documents
     * @param catalogClient HTTP client for product data from catalog-service
     */
    public CartService(CartRepository cartRepository, CartMapper cartMapper, CatalogClient catalogClient) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.catalogClient = catalogClient;
    }

    /**
     * Returns a customer's active cart, creating an empty cart if needed.
     *
     * @param customerId customer ID
     * @return cart response
     */
    public CartResponse getCart(String customerId) {
        log.debug("Fetching cart customerId={}", customerId);
        return cartMapper.toResponse(findOrCreateCart(customerId));
    }

    /**
     * Adds an item to the customer's cart.
     *
     * <p>If the same product already exists in the cart, this method increases
     * the quantity instead of adding a duplicate item row. Product details are
     * loaded from catalog-service so callers cannot inject product names or
     * prices into the cart.</p>
     *
     * @param customerId customer ID
     * @param request validated add-cart-item request
     * @return updated cart response
     */
    public CartResponse addItem(String customerId, AddCartItemRequest request) {
        log.info("Adding cart item customerId={} productId={} quantity={}",
                customerId, request.productId(), request.quantity());
        CatalogProductResponse product = catalogClient.getProduct(request.productId());
        CartDocument cart = findOrCreateCart(customerId);
        Optional<CartItemDocument> existingItem = findItem(cart, request.productId());

        if (existingItem.isPresent()) {
            CartItemDocument item = existingItem.get();
            cartMapper.refreshItemSnapshot(item, product);
            item.setQuantity(item.getQuantity() + request.quantity());
            log.debug("Incremented existing cart item customerId={} productId={} newQuantity={}",
                    customerId, request.productId(), item.getQuantity());
        } else {
            cart.getItems().add(cartMapper.toItemDocument(product, request.quantity()));
            log.debug("Added new cart item customerId={} productId={}", customerId, request.productId());
        }

        CartDocument savedCart = cartRepository.save(cart);
        CartResponse response = cartMapper.toResponse(savedCart);
        log.info("Cart item added customerId={} cartId={} totalItems={} subtotal={}",
                customerId, response.cartId(), response.totalItems(), response.subtotal());
        return response;
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
        log.info("Updating cart item quantity customerId={} productId={} quantity={}",
                customerId, productId, request.quantity());
        CartDocument cart = findOrCreateCart(customerId);
        CartItemDocument item = findItem(cart, productId)
                .orElseThrow(() -> {
                    log.warn("Cart item quantity update failed because product is missing customerId={} productId={}",
                            customerId, productId);
                    return new CartItemNotFoundException("Product not found in cart: " + productId);
                });

        item.setQuantity(request.quantity());
        CartResponse response = cartMapper.toResponse(cartRepository.save(cart));
        log.info("Cart item quantity updated customerId={} productId={} totalItems={}",
                customerId, productId, response.totalItems());
        return response;
    }

    /**
     * Removes one item from a customer's cart.
     *
     * @param customerId customer ID
     * @param productId product ID
     * @return updated cart response
     */
    public CartResponse removeItem(String customerId, String productId) {
        log.info("Removing cart item customerId={} productId={}", customerId, productId);
        CartDocument cart = findOrCreateCart(customerId);
        int itemIndex = findItemIndex(cart, productId);
        cart.getItems().remove(itemIndex);
        CartResponse response = cartMapper.toResponse(cartRepository.save(cart));
        log.info("Cart item removed customerId={} productId={} totalItems={}",
                customerId, productId, response.totalItems());
        return response;
    }

    /**
     * Clears all items from a customer's cart.
     *
     * @param customerId customer ID
     */
    public void clearCart(String customerId) {
        log.info("Clearing cart customerId={}", customerId);
        CartDocument cart = findOrCreateCart(customerId);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cart cleared customerId={} cartId={}", customerId, cart.getId());
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
        CartDocument savedCart = cartRepository.save(cart);
        log.info("Created new cart customerId={} cartId={}", customerId, savedCart.getId());
        return savedCart;
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
                .orElseThrow(() -> {
                    log.warn("Cart item not found cartId={} customerId={} productId={}",
                            cart.getId(), cart.getCustomerId(), productId);
                    return new CartItemNotFoundException("Product not found in cart: " + productId);
                });
    }
}
