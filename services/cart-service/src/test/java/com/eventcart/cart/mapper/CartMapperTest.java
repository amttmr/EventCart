package com.eventcart.cart.mapper;

import com.eventcart.cart.domain.CartDocument;
import com.eventcart.cart.domain.CartItemDocument;
import com.eventcart.cart.dto.AddCartItemRequest;
import com.eventcart.cart.dto.CartResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CartMapper}.
 */
class CartMapperTest {
    private final CartMapper cartMapper = new CartMapper();

    /**
     * Verifies that add-item requests become embedded cart item documents.
     */
    @Test
    void toItemDocumentShouldMapAddRequest() {
        AddCartItemRequest request = new AddCartItemRequest(
                "product-1",
                "SKU-1",
                "Mechanical Keyboard",
                new BigDecimal("6999.00"),
                "inr",
                2
        );

        CartItemDocument item = cartMapper.toItemDocument(request);

        assertThat(item.getProductId()).isEqualTo("product-1");
        assertThat(item.getCurrency()).isEqualTo("INR");
        assertThat(item.getQuantity()).isEqualTo(2);
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
}

