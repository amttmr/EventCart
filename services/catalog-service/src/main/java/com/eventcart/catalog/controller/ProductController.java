package com.eventcart.catalog.controller;

import com.eventcart.catalog.dto.CreateProductRequest;
import com.eventcart.catalog.dto.ProductResponse;
import com.eventcart.catalog.dto.ProductSearchCriteria;
import com.eventcart.catalog.dto.UpdateProductRequest;
import com.eventcart.catalog.service.ProductService;
import com.eventcart.common.web.ApiResponse;
import com.eventcart.common.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + product.id()))
                .body(ApiResponse.success(product, "Product created"));
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable String productId) {
        return ApiResponse.success(productService.getProduct(productId));
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                keyword,
                category,
                active,
                minPrice,
                maxPrice
        );
        Page<ProductResponse> page = productService.searchProducts(criteria, pageable);
        return ApiResponse.success(PageResponse.from(page));
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ApiResponse.success(productService.updateProduct(productId, request), "Product updated");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable String productId) {
        productService.deactivateProduct(productId);
        return ResponseEntity.noContent().build();
    }
}

