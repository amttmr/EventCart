package com.eventcart.catalog.mapper;

import com.eventcart.catalog.domain.ProductDocument;
import com.eventcart.catalog.dto.CreateProductRequest;
import com.eventcart.catalog.dto.ProductResponse;
import com.eventcart.catalog.dto.UpdateProductRequest;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
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

