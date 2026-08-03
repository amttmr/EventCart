package com.eventcart.order.client;

import com.eventcart.common.web.ApiResponse;
import com.eventcart.order.exception.CartServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

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
    private final CartClientProperties properties;

    /**
     * Creates a cart client.
     *
     * @param cartRestClient RestClient configured for cart-service
     * @param properties cart-service client settings
     */
    public CartClient(RestClient cartRestClient, CartClientProperties properties) {
        this.cartRestClient = cartRestClient;
        this.properties = properties;
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
                    .headers(headers -> currentBearerToken().ifPresent(headers::setBearerAuth))
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

    /**
     * Clears the active cart for one customer.
     *
     * @param customerId customer ID
     */
    public void clearCart(String customerId) {
        log.info("Calling cart-service to clear cart customerId={}", customerId);
        try {
            cartRestClient
                    .delete()
                    .uri("/api/v1/carts/{customerId}", customerId)
                    .headers(headers -> currentBearerToken().ifPresentOrElse(
                            headers::setBearerAuth,
                            () -> applyInternalToken(headers)
                    ))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        log.warn("Cart clear rejected by cart-service customerId={} status={}",
                                customerId, clientResponse.getStatusCode());
                        throw new CartServiceUnavailableException("Cart service rejected cart clear for customer: " + customerId);
                    })
                    .toBodilessEntity();
            log.info("Cart clear completed customerId={}", customerId);
        } catch (CartServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.warn("Cart clear call failed customerId={}", customerId, ex);
            throw new CartServiceUnavailableException("Cart service is unavailable", ex);
        }
    }

    /**
     * Adds the configured internal token for asynchronous cart cleanup calls.
     *
     * @param headers outgoing request headers
     */
    private void applyInternalToken(HttpHeaders headers) {
        if (properties.internalToken() != null && !properties.internalToken().isBlank()) {
            headers.set(properties.internalHeaderName(), properties.internalToken());
        }
    }

    /**
     * Reads the incoming bearer token so customer ownership checks work across service-to-service HTTP calls.
     *
     * @return bearer token without the {@code Bearer } prefix when the current request has one
     */
    private Optional<String> currentBearerToken() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return Optional.empty();
        }

        String authorizationHeader = servletRequestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(authorizationHeader.substring("Bearer ".length()));
    }
}
