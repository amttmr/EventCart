package com.eventcart.gateway.observability;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Gateway filter that propagates an {@code X-Correlation-Id} header.
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /**
     * Adds or preserves a correlation ID before routing downstream.
     *
     * @param exchange current server exchange
     * @param chain gateway filter chain
     * @return completion signal
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();
        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);
        return chain.filter(exchange.mutate().request(request).build());
    }

    /**
     * Runs this filter early so all downstream filters see the header.
     *
     * @return filter order
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
