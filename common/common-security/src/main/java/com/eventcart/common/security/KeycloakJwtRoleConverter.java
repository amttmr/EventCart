package com.eventcart.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Converts Keycloak JWT role claims into Spring Security authorities.
 *
 * <p>Keycloak places realm roles under {@code realm_access.roles}. Spring
 * Security expects role authorities to be prefixed with {@code ROLE_}, so this
 * converter bridges that representation while preserving standard scope
 * authorities.</p>
 */
public class KeycloakJwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    /**
     * Converts a JWT into authorities understood by Spring Security.
     *
     * @param jwt authenticated JWT
     * @return merged scope and Keycloak role authorities
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(scopeConverter.convert(jwt));
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) {
            return authorities;
        }

        Object roles = realmAccess.get("roles");
        if (roles instanceof Collection<?> roleNames) {
            roleNames.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(role -> "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return authorities;
    }
}
