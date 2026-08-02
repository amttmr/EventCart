package com.eventcart.common.web.observability;

import org.slf4j.MDC;

/**
 * Accessor for the correlation ID stored in SLF4J MDC.
 */
public final class CorrelationIdContext {
    /**
     * HTTP header used to propagate a correlation ID between services.
     */
    public static final String HEADER_NAME = "X-Correlation-Id";

    /**
     * MDC key used in log patterns.
     */
    public static final String MDC_KEY = "correlationId";

    /**
     * Prevents creation of this utility class.
     */
    private CorrelationIdContext() {
    }

    /**
     * Returns the current correlation ID or a fallback value.
     *
     * @param fallback fallback value when no correlation ID is bound
     * @return current correlation ID or fallback value
     */
    public static String getCorrelationIdOr(String fallback) {
        String correlationId = MDC.get(MDC_KEY);
        return correlationId == null || correlationId.isBlank() ? fallback : correlationId;
    }
}
