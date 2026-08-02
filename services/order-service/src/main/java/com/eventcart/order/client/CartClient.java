package com.eventcart.order.client;

import com.eventcart.common.web.ApiResponse;
import com.eventcart.order.exception.CartServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * HTTP client used by order-service to read customer carts from cart-service.
 */
@Component
public class CartClient {
    private static final Logger log = LoggerFactory.getLogger(CartClient.class);

    private static final ParameterizedTypeReference<ApiResponse<CartResponse>> CART_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient cartRestClient;

    /**
     * Creates a cart client.
     *
     * @param cartRestClient RestClient configured for cart-service
     */
    public CartClient(RestClient cartRestClient) {
        this.cartRestClient = cartRestClient;
    }

    /**
     * Fetches the active cart for one customer.
     *
     * @param customerId customer ID
     * @return cart data returned by cart-service
     */
    public CartResponse getCart(String customerId) {
        log.debug("Calling cart-service for customer cart customerId={}", customerId);
        try {
            ApiResponse<CartResponse> response = cartRestClient
                    .get()
                    .uri("/api/v1/carts/{customerId}", customerId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        log.warn("Cart lookup rejected by cart-service customerId={} status={}",
                                customerId, clientResponse.getStatusCode());
                        throw new CartServiceUnavailableException("Cart service rejected cart lookup for customer: " + customerId);
                    })
                    .body(CART_RESPONSE_TYPE);

            if (response == null || response.data() == null) {
                log.warn("Cart lookup returned empty body customerId={}", customerId);
                throw new CartServiceUnavailableException("Cart service returned an empty response");
            }

            log.debug("Cart lookup succeeded customerId={} cartId={} itemCount={}",
                    customerId, response.data().cartId(), response.data().items().size());
            return response.data();
        } catch (CartServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.warn("Cart service call failed customerId={}", customerId, ex);
            throw new CartServiceUnavailableException("Cart service is unavailable", ex);
        }
    }
}
