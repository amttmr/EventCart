package com.eventcart.order.exception;

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
 * Converts order-service exceptions into stable API error responses.
 */
@RestControllerAdvice
public class OrderExceptionHandler {
    /**
     * Handles missing orders.
     *
     * @param ex order-not-found exception
     * @param request current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleOrderNotFound(OrderNotFoundException ex, HttpServletRequest request) {
        return ApiError.of("ORDER_NOT_FOUND", ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles attempts to place an order from an empty cart.
     *
     * @param ex empty-cart exception
     * @param request current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(EmptyCartException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEmptyCart(EmptyCartException ex, HttpServletRequest request) {
        return ApiError.of("EMPTY_CART", ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles failures while calling cart-service.
     *
     * @param ex cart-service unavailable exception
     * @param request current HTTP request
     * @return standardized API error response
     */
    @ExceptionHandler(CartServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleCartUnavailable(CartServiceUnavailableException ex, HttpServletRequest request) {
        return ApiError.of("CART_SERVICE_UNAVAILABLE", ex.getMessage(), request.getRequestURI());
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
