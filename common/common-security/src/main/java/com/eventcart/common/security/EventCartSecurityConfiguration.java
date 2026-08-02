package com.eventcart.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;

/**
 * Shared security rules for EventCart servlet-based services.
 *
 * <p>The gateway is the preferred public entry point, but each backend service
 * also protects its APIs so direct port access behaves consistently during
 * local development and interviews.</p>
 */
@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication(type = Type.SERVLET)
public class EventCartSecurityConfiguration {
    /**
     * Creates the servlet security filter chain used by backend services.
     *
     * @param http Spring Security HTTP builder
     * @param jwtAuthenticationConverter JWT to authentication converter
     * @return configured security filter chain
     * @throws Exception when Spring Security cannot build the chain
     */
    @Bean
    @ConditionalOnProperty(prefix = "eventcart.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain eventCartSecurityFilterChain(
            HttpSecurity http,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health/**",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers("/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/inventory/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/carts/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/v1/orders/**").hasAnyRole("CUSTOMER", "ADMIN", "SUPPORT")
                        .requestMatchers("/api/v1/payments/**").hasAnyRole("ADMIN", "SUPPORT")
                        .requestMatchers("/api/v1/notifications/**").hasAnyRole("CUSTOMER", "ADMIN", "SUPPORT")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Creates a permissive chain for tests that intentionally disable security.
     *
     * @param http Spring Security HTTP builder
     * @return filter chain that permits every request
     * @throws Exception when Spring Security cannot build the chain
     */
    @Bean
    @ConditionalOnProperty(prefix = "eventcart.security", name = "enabled", havingValue = "false")
    public SecurityFilterChain eventCartSecurityDisabledFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Creates an authentication converter that includes Keycloak realm roles.
     *
     * @param authoritiesConverter authority converter for Keycloak claims
     * @return JWT authentication converter
     */
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(
            Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter
    ) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    /**
     * Creates the reusable Keycloak role converter.
     *
     * @return converter that maps Keycloak roles to Spring authorities
     */
    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> keycloakJwtRoleConverter() {
        return new KeycloakJwtRoleConverter();
    }
}
