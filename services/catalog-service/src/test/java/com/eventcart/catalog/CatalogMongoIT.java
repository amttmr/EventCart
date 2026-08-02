package com.eventcart.catalog;

import com.eventcart.catalog.domain.ProductDocument;
import com.eventcart.catalog.repository.ProductRepository;
import com.eventcart.common.test.TestProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testcontainers-backed integration test for catalog MongoDB persistence.
 */
@SpringBootTest
@ActiveProfiles(TestProfiles.INTEGRATION_TEST)
@Testcontainers(disabledWithoutDocker = true)
class CatalogMongoIT {
    @Container
    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

    @Autowired
    private ProductRepository productRepository;

    /**
     * Registers container-backed MongoDB properties for Spring Boot.
     *
     * @param registry dynamic property registry
     */
    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("eventcart.security.enabled", () -> "false");
    }

    /**
     * Verifies that catalog-service can persist and read a product using real MongoDB.
     */
    @Test
    void shouldPersistAndReadProductWithMongoContainer() {
        ProductDocument product = new ProductDocument();
        product.setSku("IT-SKU-1001");
        product.setName("Integration Test Keyboard");
        product.setDescription("Keyboard stored by a Testcontainers MongoDB test");
        product.setCategory("Electronics");
        product.setPrice(new BigDecimal("6999.00"));
        product.setCurrency("INR");
        product.setAvailableQuantity(10);
        product.setTags(List.of("testcontainers", "mongodb"));
        product.setActive(true);

        ProductDocument saved = productRepository.save(product);

        assertThat(saved.getId()).isNotBlank();
        assertThat(productRepository.findById(saved.getId())).isPresent();
    }
}
