package com.eventcart.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Validates narrowly scoped internal service credentials for server-side workflow calls.
 *
 * <p>The public API continues to use JWT and role checks. This policy exists for
 * asynchronous service-to-service calls that do not have an end-user HTTP request,
 * such as order-service clearing a cart after consuming an inventory event.</p>
 */
@Component
public class InternalServiceAccessPolicy {
    private final String headerName;
    private final String token;

    /**
     * Creates an internal service access policy.
     *
     * @param headerName request header that carries the shared internal token
     * @param token configured shared token; blank values disable internal access
     */
    public InternalServiceAccessPolicy(
            @Value("${eventcart.internal-service.header-name:X-EventCart-Internal-Token}") String headerName,
            @Value("${eventcart.internal-service.token:}") String token
    ) {
        this.headerName = StringUtils.hasText(headerName) ? headerName : "X-EventCart-Internal-Token";
        this.token = token;
    }

    /**
     * Returns true when the request is the supported internal cart-clear call and has a valid token.
     *
     * @param request HTTP request to evaluate
     * @return true when the request can bypass the JWT role rule
     */
    public boolean isAllowedInternalRequest(HttpServletRequest request) {
        return isCartClearRequest(request) && hasValidToken(request);
    }

    /**
     * Returns true when the current request is a supported internal request with a valid token.
     *
     * @return true when the current internal request is allowed
     */
    public boolean isAllowedCurrentInternalRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return false;
        }
        return isAllowedInternalRequest(servletRequestAttributes.getRequest());
    }

    /**
     * Compares the incoming token with the configured token using a constant-time byte comparison.
     *
     * @param request HTTP request that may contain the internal token header
     * @return true when the configured and incoming tokens match
     */
    public boolean hasValidToken(HttpServletRequest request) {
        if (!StringUtils.hasText(token) || request == null) {
            return false;
        }
        String candidate = request.getHeader(headerName);
        if (!StringUtils.hasText(candidate)) {
            return false;
        }
        byte[] configuredToken = token.getBytes(StandardCharsets.UTF_8);
        byte[] candidateToken = candidate.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(configuredToken, candidateToken);
    }

    /**
     * Checks whether the request targets the internal cart cleanup endpoint.
     *
     * @param request HTTP request to evaluate
     * @return true when the request is DELETE /api/v1/carts/{customerId}
     */
    private boolean isCartClearRequest(HttpServletRequest request) {
        if (request == null || !HttpMethod.DELETE.matches(request.getMethod())) {
            return false;
        }
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        return requestPath.matches("/api/v1/carts/[^/]+");
    }
}
