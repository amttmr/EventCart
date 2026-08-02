package com.eventcart.catalog.controller;

import com.eventcart.catalog.dto.CreateProductRequest;
import com.eventcart.catalog.dto.ProductResponse;
import com.eventcart.catalog.dto.ProductSearchCriteria;
import com.eventcart.catalog.dto.UpdateProductRequest;
import com.eventcart.catalog.service.ProductService;
import com.eventcart.common.web.ApiResponse;
import com.eventcart.common.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST controller that exposes product catalog APIs.
 *
 * <p>The controller translates HTTP requests into validated DTOs and delegates
 * business behavior to {@link ProductService}.</p>
 */
@Tag(name = "Products", description = "Product catalog management and search APIs")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    /**
     * Creates a product controller.
     *
     * @param productService catalog business service
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a new product.
     *
     * @param request validated product creation request
     * @return HTTP 201 response containing the created product
     */
    @Operation(summary = "Create product", description = "Creates a product in the catalog with SKU, price, quantity, category, and tags.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "SKU already exists")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product details to create in catalog-service",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Electronics product",
                                            summary = "Mechanical keyboard",
                                            value = """
                                                    {
                                                      "sku": "SKU-1001",
                                                      "name": "Mechanical Keyboard",
                                                      "description": "Hot-swappable mechanical keyboard with RGB lighting",
                                                      "category": "Electronics",
                                                      "price": 6999.00,
                                                      "currency": "INR",
                                                      "availableQuantity": 25,
                                                      "tags": ["keyboard", "gaming", "rgb"]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Book product",
                                            summary = "Interview preparation book",
                                            value = """
                                                    {
                                                      "sku": "BOOK-2001",
                                                      "name": "Java Microservices Interview Guide",
                                                      "description": "Backend interview preparation book for Java and Spring Boot",
                                                      "category": "Books",
                                                      "price": 1499.00,
                                                      "currency": "INR",
                                                      "availableQuantity": 100,
                                                      "tags": ["java", "spring", "interview"]
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + product.id()))
                .body(ApiResponse.success(product, "Product created"));
    }

    /**
     * Retrieves a product by ID.
     *
     * @param productId MongoDB product ID
     * @return product response wrapped in the standard API envelope
     */
    @Operation(summary = "Get product", description = "Returns one product by its catalog ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @Parameter(description = "MongoDB product ID", example = "6a6f2ff6c33ef72269887fec") @PathVariable String productId
    ) {
        return ApiResponse.success(productService.getProduct(productId));
    }

    /**
     * Searches products using optional filters.
     *
     * @param keyword optional keyword matched against name, description, and tags
     * @param category optional product category
     * @param active optional active/inactive flag
     * @param minPrice optional minimum price
     * @param maxPrice optional maximum price
     * @param pageable pagination and sorting values resolved by Spring MVC
     * @return paginated product response
     */
    @Operation(summary = "Search products", description = "Searches products using optional keyword, category, active flag, and price range filters.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products returned")
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @Parameter(description = "Keyword matched against name, description, and tags", example = "keyboard") @RequestParam(required = false) String keyword,
            @Parameter(description = "Product category", example = "Electronics") @RequestParam(required = false) String category,
            @Parameter(description = "Whether the product is active", example = "true") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Minimum product price", example = "1000.00") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum product price", example = "10000.00") @RequestParam(required = false) BigDecimal maxPrice,
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

    /**
     * Updates an existing product.
     *
     * @param productId MongoDB product ID
     * @param request validated product update request
     * @return updated product response
     */
    @Operation(summary = "Update product", description = "Updates product details such as name, category, price, quantity, tags, and active flag.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @Parameter(description = "MongoDB product ID", example = "6a6f2ff6c33ef72269887fec") @PathVariable String productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Complete product state to save",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Update price and stock",
                                            summary = "Keep product active",
                                            value = """
                                                    {
                                                      "name": "Mechanical Keyboard Pro",
                                                      "description": "Hot-swappable keyboard with RGB lighting and wireless mode",
                                                      "category": "Electronics",
                                                      "price": 7999.00,
                                                      "currency": "INR",
                                                      "availableQuantity": 30,
                                                      "tags": ["keyboard", "gaming", "wireless"],
                                                      "active": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Temporarily hide product",
                                            summary = "Set active false",
                                            value = """
                                                    {
                                                      "name": "Mechanical Keyboard Pro",
                                                      "description": "Temporarily unavailable product",
                                                      "category": "Electronics",
                                                      "price": 7999.00,
                                                      "currency": "INR",
                                                      "availableQuantity": 0,
                                                      "tags": ["keyboard", "gaming", "wireless"],
                                                      "active": false
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ApiResponse.success(productService.updateProduct(productId, request), "Product updated");
    }

    /**
     * Deactivates an existing product.
     *
     * @param productId MongoDB product ID
     * @return HTTP 204 response when deactivation succeeds
     */
    @Operation(summary = "Deactivate product", description = "Marks a product inactive without physically deleting it.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Product deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deactivateProduct(
            @Parameter(description = "MongoDB product ID", example = "6a6f2ff6c33ef72269887fec") @PathVariable String productId
    ) {
        productService.deactivateProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
