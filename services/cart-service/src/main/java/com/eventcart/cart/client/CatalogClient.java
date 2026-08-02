package com.eventcart.cart.client;

import com.eventcart.cart.exception.CatalogServiceUnavailableException;
import com.eventcart.cart.exception.ProductNotAvailableException;
import com.eventcart.common.web.ApiResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * HTTP client used by cart-service to read product data from catalog-service.
 */
@Component
public class CatalogClient {
    private static final ParameterizedTypeReference<ApiResponse<CatalogProductResponse>> PRODUCT_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient catalogRestClient;

    /**
     * Creates a catalog client.
     *
     * @param catalogRestClient RestClient configured for catalog-service
     */
    public CatalogClient(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    /**
     * Fetches one active product from catalog-service.
     *
     * @param productId product ID to fetch
     * @return product data returned by catalog-service
     */
    public CatalogProductResponse getProduct(String productId) {
        try {
            ApiResponse<CatalogProductResponse> response = catalogRestClient
                    .get()
                    .uri("/api/v1/products/{productId}", productId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        if (clientResponse.getStatusCode().value() == 404) {
                            throw new ProductNotAvailableException("Product not found in catalog: " + productId);
                        }
                        throw new ProductNotAvailableException("Catalog rejected product lookup: " + productId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, clientResponse) -> {
                        throw new CatalogServiceUnavailableException("Catalog service failed while looking up product: " + productId);
                    })
                    .body(PRODUCT_RESPONSE_TYPE);

            if (response == null || response.data() == null) {
                throw new CatalogServiceUnavailableException("Catalog service returned an empty product response");
            }

            CatalogProductResponse product = response.data();
            if (!product.active()) {
                throw new ProductNotAvailableException("Product is inactive in catalog: " + productId);
            }

            return product;
        } catch (ProductNotAvailableException | CatalogServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new CatalogServiceUnavailableException("Catalog service is unavailable", ex);
        }
    }
}

