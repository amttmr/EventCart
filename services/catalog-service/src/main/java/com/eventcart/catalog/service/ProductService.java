package com.eventcart.catalog.service;

import com.eventcart.catalog.domain.ProductDocument;
import com.eventcart.catalog.dto.CreateProductRequest;
import com.eventcart.catalog.dto.ProductResponse;
import com.eventcart.catalog.dto.ProductSearchCriteria;
import com.eventcart.catalog.dto.UpdateProductRequest;
import com.eventcart.catalog.exception.DuplicateResourceException;
import com.eventcart.catalog.exception.ResourceNotFoundException;
import com.eventcart.catalog.mapper.ProductMapper;
import com.eventcart.catalog.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            MongoTemplate mongoTemplate,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
        this.productMapper = productMapper;
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product SKU already exists: " + request.sku());
        }

        ProductDocument savedProduct = productRepository.save(productMapper.toDocument(request));
        return productMapper.toResponse(savedProduct);
    }

    public ProductResponse getProduct(String productId) {
        return productMapper.toResponse(findProduct(productId));
    }

    public Page<ProductResponse> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Query query = buildSearchQuery(criteria).with(pageable);
        Query countQuery = buildSearchQuery(criteria);

        long total = mongoTemplate.count(countQuery, ProductDocument.class);
        List<ProductResponse> products = mongoTemplate.find(query, ProductDocument.class)
                .stream()
                .map(productMapper::toResponse)
                .toList();

        return new PageImpl<>(products, pageable, total);
    }

    public ProductResponse updateProduct(String productId, UpdateProductRequest request) {
        ProductDocument product = findProduct(productId);
        productMapper.updateDocument(product, request);
        return productMapper.toResponse(productRepository.save(product));
    }

    public void deactivateProduct(String productId) {
        ProductDocument product = findProduct(productId);
        product.setActive(false);
        productRepository.save(product);
    }

    private ProductDocument findProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private Query buildSearchQuery(ProductSearchCriteria criteria) {
        List<Criteria> filters = new ArrayList<>();

        if (StringUtils.hasText(criteria.keyword())) {
            Pattern keywordPattern = Pattern.compile(Pattern.quote(criteria.keyword()), Pattern.CASE_INSENSITIVE);
            filters.add(new Criteria().orOperator(
                    Criteria.where("name").regex(keywordPattern),
                    Criteria.where("description").regex(keywordPattern),
                    Criteria.where("tags").regex(keywordPattern)
            ));
        }

        if (StringUtils.hasText(criteria.category())) {
            filters.add(Criteria.where("category").is(criteria.category()));
        }

        if (criteria.active() != null) {
            filters.add(Criteria.where("active").is(criteria.active()));
        }

        BigDecimal minPrice = criteria.minPrice();
        BigDecimal maxPrice = criteria.maxPrice();

        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (minPrice != null) {
                priceCriteria = priceCriteria.gte(minPrice);
            }
            if (maxPrice != null) {
                priceCriteria = priceCriteria.lte(maxPrice);
            }
            filters.add(priceCriteria);
        }

        Query query = new Query();
        if (!filters.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filters.toArray(Criteria[]::new)));
        }
        return query;
    }
}
