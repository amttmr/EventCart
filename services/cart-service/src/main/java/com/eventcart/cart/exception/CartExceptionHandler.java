package com.eventcart.cart.exception;

import com.eventcart.common.web.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts cart-service exceptions into stable API error responses.
 */
@RestControllerAdvice
public class CartExceptionHandler {
    /**
     * Handles missing cart items.
     *
     * @param ex cart-item not-found exception
     * @param request current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(CartItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleCartItemNotFound(CartItemNotFoundException ex, HttpServletRequest request) {
        return ApiError.of("CART_ITEM_NOT_FOUND", ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles products that catalog-service cannot provide for cart usage.
     *
     * @param ex product-not-available exception
     * @param request current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(ProductNotAvailableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleProductNotAvailable(ProductNotAvailableException ex, HttpServletRequest request) {
        return ApiError.of("PRODUCT_NOT_AVAILABLE", ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles failures while calling catalog-service.
     *
     * @param ex catalog-service unavailable exception
     * @param request current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(CatalogServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleCatalogUnavailable(CatalogServiceUnavailableException ex, HttpServletRequest request) {
        return ApiError.of("CATALOG_SERVICE_UNAVAILABLE", ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles request validation failures.
     *
     * @param ex validation exception raised by Spring MVC
     * @param request current HTTP request
     * @return standardized API error response with field-level details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() == null
                                ? "Invalid value"
                                : fieldError.getDefaultMessage(),
                        (first, second) -> first
                ));

        return ApiError.of("VALIDATION_FAILED", "Request validation failed", request.getRequestURI(), details);
    }

    /**
     * Handles missing or malformed JSON request bodies.
     *
     * @param ex JSON parsing exception raised by Spring MVC
     * @param request current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ApiError.of("MALFORMED_REQUEST", "Request body is missing or malformed", request.getRequestURI());
    }

    /**
     * Handles unexpected failures that were not mapped more specifically.
     *
     * @param ex unexpected exception
     * @param request current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception ex, HttpServletRequest request) {
        return ApiError.of("INTERNAL_ERROR", "Unexpected server error", request.getRequestURI());
    }
}
