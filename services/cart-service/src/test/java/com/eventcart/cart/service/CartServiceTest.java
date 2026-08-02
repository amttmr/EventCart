package com.eventcart.cart.service;

import com.eventcart.cart.client.CatalogClient;
import com.eventcart.cart.client.CatalogProductResponse;
import com.eventcart.cart.domain.CartDocument;
import com.eventcart.cart.dto.AddCartItemRequest;
import com.eventcart.cart.dto.CartResponse;
import com.eventcart.cart.mapper.CartMapper;
import com.eventcart.cart.repository.CartRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CartService}.
 */
class CartServiceTest {
    private final CartRepository cartRepository = mock(CartRepository.class);
    private final CartMapper cartMapper = new CartMapper();
    private final CatalogClient catalogClient = mock(CatalogClient.class);
    private final CartService cartService = new CartService(cartRepository, cartMapper, catalogClient);

    /**
     * Verifies that adding an item fetches product data from catalog-service and
     * stores a catalog-derived snapshot in the cart.
     */
    @Test
    void addItemShouldFetchCatalogProductAndStoreSnapshot() {
        CatalogProductResponse product = product("product-1");
        CartDocument cart = new CartDocument();
        cart.setId("cart-1");
        cart.setCustomerId("customer-1");

        when(catalogClient.getProduct("product-1")).thenReturn(product);
        when(cartRepository.findByCustomerId("customer-1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.addItem("customer-1", new AddCartItemRequest("product-1", 2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productId()).isEqualTo("product-1");
        assertThat(response.items().getFirst().productName()).isEqualTo("Mechanical Keyboard");
        assertThat(response.items().getFirst().unitPrice()).isEqualByComparingTo("6999.00");
        assertThat(response.totalItems()).isEqualTo(2);
        verify(catalogClient).getProduct("product-1");
        verify(cartRepository).save(cart);
    }

    /**
     * Creates a catalog product response for service tests.
     *
     * @param productId product ID
     * @return catalog product response
     */
    private CatalogProductResponse product(String productId) {
        return new CatalogProductResponse(
                productId,
                "SKU-1",
                "Mechanical Keyboard",
                "Test product",
                "Electronics",
                new BigDecimal("6999.00"),
                "INR",
                10,
                List.of("keyboard"),
                true,
                0L,
                Instant.now(),
                Instant.now()
        );
    }
}

