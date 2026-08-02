package com.eventcart.catalog.mapper;

import com.eventcart.catalog.domain.ProductDocument;
import com.eventcart.catalog.dto.CreateProductRequest;
import com.eventcart.catalog.dto.ProductResponse;
import com.eventcart.catalog.dto.UpdateProductRequest;
import org.springframework.stereotype.Component;

/**
 * Maps between product DTOs and MongoDB product documents.
 *
 * <p>Keeping mapping code out of controllers and services makes the API layer,
 * business layer, and persistence model easier to change independently.</p>
 */
@Component
public class ProductMapper {
    /**
     * Converts a create request into a new product document.
     *
     * @param request validated create-product request
     * @return product document ready to be persisted
     */
    public ProductDocument toDocument(CreateProductRequest request) {
        ProductDocument product = new ProductDocument();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setCurrency(request.currency().toUpperCase());
        product.setAvailableQuantity(request.availableQuantity());
        product.setTags(request.tags());
        product.setActive(true);
        return product;
    }

    /**
     * Applies update request values to an existing product document.
     *
     * @param product existing product document loaded from MongoDB
     * @param request validated update-product request
     */
    public void updateDocument(ProductDocument product, UpdateProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setCurrency(request.currency().toUpperCase());
        product.setAvailableQuantity(request.availableQuantity());
        product.setTags(request.tags());
        product.setActive(request.active());
    }

    /**
     * Converts a product document into the public API response shape.
     *
     * @param product persisted product document
     * @return product response returned by REST APIs
     */
    public ProductResponse toResponse(ProductDocument product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getCurrency(),
                product.getAvailableQuantity(),
                product.getTags(),
                product.isActive(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
