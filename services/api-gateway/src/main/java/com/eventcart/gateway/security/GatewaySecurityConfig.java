package com.eventcart.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Reactive JWT security configuration for the API Gateway.
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    /**
     * Creates the gateway security filter chain.
     *
     * @param http reactive HTTP security builder
     * @param jwtAuthenticationConverter reactive JWT converter
     * @return configured security filter chain
     */
    @Bean
    public SecurityWebFilterChain gatewaySecurityWebFilterChain(
            ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(
                                "/actuator/health/**",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .pathMatchers("/api/v1/products/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/inventory/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/carts/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers("/api/v1/orders/**").hasAnyRole("CUSTOMER", "ADMIN", "SUPPORT")
                        .pathMatchers(HttpMethod.GET, "/api/v1/payments/orders/**")
                        .hasAnyRole("CUSTOMER", "ADMIN", "SUPPORT")
                        .pathMatchers("/api/v1/payments/**").hasAnyRole("ADMIN", "SUPPORT")
                        .pathMatchers("/api/v1/notifications/**").hasAnyRole("CUSTOMER", "ADMIN", "SUPPORT")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    /**
     * Creates the reactive JWT authentication converter.
     *
     * @return converter that includes Keycloak realm roles
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(new GatewayKeycloakJwtRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }
}
