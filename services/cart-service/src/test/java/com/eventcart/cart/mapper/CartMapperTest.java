package com.eventcart.cart.mapper;

import com.eventcart.cart.client.CatalogProductResponse;
import com.eventcart.cart.domain.CartDocument;
import com.eventcart.cart.domain.CartItemDocument;
import com.eventcart.cart.dto.CartResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CartMapper}.
 */
class CartMapperTest {
    private final CartMapper cartMapper = new CartMapper();

    /**
     * Verifies that catalog product responses become embedded cart item documents.
     */
    @Test
    void toItemDocumentShouldMapCatalogProduct() {
        CatalogProductResponse product = product("product-1", "SKU-1", "Mechanical Keyboard", "inr");

        CartItemDocument item = cartMapper.toItemDocument(product, 2);

        assertThat(item.getProductId()).isEqualTo("product-1");
        assertThat(item.getCurrency()).isEqualTo("INR");
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    /**
     * Verifies that an existing cart item can refresh its snapshot from catalog data.
     */
    @Test
    void refreshItemSnapshotShouldUpdateExistingItemDetails() {
        CartItemDocument item = cartMapper.toItemDocument(
                product("product-1", "SKU-OLD", "Old Keyboard", "INR"),
                1
        );
        CatalogProductResponse updatedProduct = product("product-1", "SKU-NEW", "New Keyboard", "usd");

        cartMapper.refreshItemSnapshot(item, updatedProduct);

        assertThat(item.getSku()).isEqualTo("SKU-NEW");
        assertThat(item.getProductName()).isEqualTo("New Keyboard");
        assertThat(item.getCurrency()).isEqualTo("USD");
        assertThat(item.getQuantity()).isEqualTo(1);
    }

    /**
     * Verifies that cart totals are calculated from embedded items.
     */
    @Test
    void toResponseShouldCalculateTotals() {
        CartItemDocument item = new CartItemDocument();
        item.setProductId("product-1");
        item.setSku("SKU-1");
        item.setProductName("Mechanical Keyboard");
        item.setUnitPrice(new BigDecimal("6999.00"));
        item.setCurrency("INR");
        item.setQuantity(2);

        CartDocument cart = new CartDocument();
        cart.setId("cart-1");
        cart.setCustomerId("customer-1");
        cart.setItems(List.of(item));

        CartResponse response = cartMapper.toResponse(cart);

        assertThat(response.totalItems()).isEqualTo(2);
        assertThat(response.subtotal()).isEqualByComparingTo("13998.00");
        assertThat(response.currency()).isEqualTo("INR");
    }

    /**
     * Creates a catalog product response for mapper tests.
     *
     * @param productId product ID
     * @param sku product SKU
     * @param name product name
     * @param currency currency code
     * @return catalog product response
     */
    private CatalogProductResponse product(String productId, String sku, String name, String currency) {
        return new CatalogProductResponse(
                productId,
                sku,
                name,
                "Test product",
                "Electronics",
                new BigDecimal("6999.00"),
                currency,
                10,
                List.of("keyboard"),
                true,
                0L,
                Instant.now(),
                Instant.now()
        );
    }
}
