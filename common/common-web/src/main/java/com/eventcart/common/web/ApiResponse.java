package com.eventcart.common.web;

import java.time.Instant;

/**
 * Standard success response returned by EventCart REST APIs.
 *
 * <p>The wrapper makes service responses consistent without exposing internal
 * domain or persistence objects directly.</p>
 *
 * @param success whether the request completed successfully
 * @param data response payload
 * @param message short response message
 * @param timestamp time when the response was created
 * @param <T> response payload type
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Instant timestamp
) {
    /**
     * Creates a successful response with the default success message.
     *
     * @param data response payload
     * @param <T> response payload type
     * @return successful API response
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "success", Instant.now());
    }

    /**
     * Creates a successful response with a custom message.
     *
     * @param data response payload
     * @param message short response message
     * @param <T> response payload type
     * @return successful API response
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now());
    }
}
