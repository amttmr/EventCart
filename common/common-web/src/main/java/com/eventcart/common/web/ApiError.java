package com.eventcart.common.web;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        String code,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> details
) {
    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, path, Instant.now(), Map.of());
    }

    public static ApiError of(String code, String message, String path, Map<String, String> details) {
        return new ApiError(code, message, path, Instant.now(), details);
    }
}

