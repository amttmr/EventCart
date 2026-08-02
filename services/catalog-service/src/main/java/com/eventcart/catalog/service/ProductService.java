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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Application service that owns product catalog business operations.
 *
 * <p>The controller layer delegates to this class so validation, lookup,
 * duplicate checks, MongoDB query construction, and persistence behavior remain
 * outside HTTP-specific code.</p>
 */
@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final ProductMapper productMapper;

    /**
     * Creates a product service with repository, MongoTemplate, and mapper dependencies.
     *
     * @param productRepository repository for standard product persistence
     * @param mongoTemplate MongoDB template used for dynamic search queries
     * @param productMapper mapper between DTOs and documents
     */
    public ProductService(
            ProductRepository productRepository,
            MongoTemplate mongoTemplate,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
        this.productMapper = productMapper;
    }

    /**
     * Creates a new product after enforcing SKU uniqueness.
     *
     * @param request validated create-product request
     * @return created product response
     */
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product sku={} category={} currency={} availableQuantity={}",
                request.sku(), request.category(), request.currency(), request.availableQuantity());

        if (productRepository.existsBySku(request.sku())) {
            log.warn("Product creation rejected because SKU already exists sku={}", request.sku());
            throw new DuplicateResourceException("Product SKU already exists: " + request.sku());
        }

        ProductDocument savedProduct = productRepository.save(productMapper.toDocument(request));
        log.info("Product created productId={} sku={} active={}",
                savedProduct.getId(), savedProduct.getSku(), savedProduct.isActive());
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Retrieves one product by ID.
     *
     * @param productId MongoDB product ID
     * @return product response
     */
    public ProductResponse getProduct(String productId) {
        log.debug("Fetching product productId={}", productId);
        return productMapper.toResponse(findProduct(productId));
    }

    /**
     * Searches products using optional filters and pagination.
     *
     * @param criteria search filters supplied by the controller
     * @param pageable pagination and sorting information
     * @return page of matching products
     */
    public Page<ProductResponse> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching products keyword={} category={} active={} minPrice={} maxPrice={} page={} size={}",
                criteria.keyword(),
                criteria.category(),
                criteria.active(),
                criteria.minPrice(),
                criteria.maxPrice(),
                pageable.getPageNumber(),
                pageable.getPageSize());

        Query query = buildSearchQuery(criteria).with(pageable);
        Query countQuery = buildSearchQuery(criteria);

        long total = mongoTemplate.count(countQuery, ProductDocument.class);
        List<ProductResponse> products = mongoTemplate.find(query, ProductDocument.class)
                .stream()
                .map(productMapper::toResponse)
                .toList();

        log.debug("Product search completed total={} returned={}", total, products.size());
        return new PageImpl<>(products, pageable, total);
    }

    /**
     * Updates an existing product.
     *
     * @param productId MongoDB product ID
     * @param request validated update-product request
     * @return updated product response
     */
    public ProductResponse updateProduct(String productId, UpdateProductRequest request) {
        log.info("Updating product productId={} category={} active={}", productId, request.category(), request.active());
        ProductDocument product = findProduct(productId);
        productMapper.updateDocument(product, request);
        ProductDocument savedProduct = productRepository.save(product);
        log.info("Product updated productId={} sku={} version={}",
                savedProduct.getId(), savedProduct.getSku(), savedProduct.getVersion());
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Marks a product inactive without physically deleting it.
     *
     * <p>Soft deletion keeps old references safer when carts or orders already
     * contain product snapshots.</p>
     *
     * @param productId MongoDB product ID
     */
    public void deactivateProduct(String productId) {
        log.info("Deactivating product productId={}", productId);
        ProductDocument product = findProduct(productId);
        product.setActive(false);
        ProductDocument savedProduct = productRepository.save(product);
        log.info("Product deactivated productId={} sku={}", savedProduct.getId(), savedProduct.getSku());
    }

    /**
     * Loads a product or throws a not-found exception.
     *
     * @param productId MongoDB product ID
     * @return product document
     */
    private ProductDocument findProduct(String productId) {
        log.debug("Loading product document productId={}", productId);
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found productId={}", productId);
                    return new ResourceNotFoundException("Product not found: " + productId);
                });
    }

    /**
     * Builds a dynamic MongoDB query from optional product search criteria.
     *
     * @param criteria search filters supplied by the API layer
     * @return MongoDB query containing only the requested filters
     */
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
