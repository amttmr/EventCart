package com.eventcart.notification.exception;

import com.eventcart.common.web.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps notification-service exceptions to standard API errors.
 */
@RestControllerAdvice
public class NotificationExceptionHandler {
    /**
     * Handles missing notification errors.
     *
     * @param ex not-found exception
     * @param request HTTP request that failed
     * @return standard API error
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotificationNotFoundException ex, HttpServletRequest request) {
        return ApiError.of("NOTIFICATION_NOT_FOUND", ex.getMessage(), request.getRequestURI());
    }
}
