package com.eventcart.common.web;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response returned by EventCart REST APIs.
 *
 * <p>Services use this shape to keep validation errors, not-found errors, and
 * unexpected failures predictable for API clients.</p>
 *
 * @param code stable machine-readable error code
 * @param message human-readable error summary
 * @param path request path that produced the error
 * @param timestamp time when the error response was created
 * @param details optional field-level or diagnostic details
 */
public record ApiError(
        String code,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> details
) {
    /**
     * Creates an error response without extra details.
     *
     * @param code stable machine-readable error code
     * @param message human-readable error summary
     * @param path request path that produced the error
     * @return populated API error response
     */
    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, path, Instant.now(), Map.of());
    }

    /**
     * Creates an error response with extra details.
     *
     * @param code stable machine-readable error code
     * @param message human-readable error summary
     * @param path request path that produced the error
     * @param details field-level or diagnostic details
     * @return populated API error response
     */
    public static ApiError of(String code, String message, String path, Map<String, String> details) {
        return new ApiError(code, message, path, Instant.now(), details);
    }
}
