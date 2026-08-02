package com.eventcart.catalog.mapper;

import com.eventcart.catalog.domain.ProductDocument;
import com.eventcart.catalog.dto.CreateProductRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProductMapper}.
 */
class ProductMapperTest {
    private final ProductMapper productMapper = new ProductMapper();

    /**
     * Verifies that create-product requests are mapped to product documents
     * with normalized currency and active status.
     */
    @Test
    void toDocumentShouldMapCreateRequest() {
        CreateProductRequest request = new CreateProductRequest(
                "SKU-1001",
                "Mechanical Keyboard",
                "Hot-swappable keyboard",
                "Electronics",
                new BigDecimal("6999.00"),
                "inr",
                25,
                List.of("keyboard", "gaming")
        );

        ProductDocument product = productMapper.toDocument(request);

        assertThat(product.getSku()).isEqualTo("SKU-1001");
        assertThat(product.getName()).isEqualTo("Mechanical Keyboard");
        assertThat(product.getCurrency()).isEqualTo("INR");
        assertThat(product.getTags()).containsExactly("keyboard", "gaming");
        assertThat(product.isActive()).isTrue();
    }
}
