package com.eventcart.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Central ownership policy for APIs scoped to a customer ID.
 */
@Component("customerAccessPolicy")
public class CustomerAccessPolicy {
    private final boolean securityEnabled;
    private final InternalServiceAccessPolicy internalServiceAccessPolicy;

    /**
     * Creates the customer ownership policy.
     *
     * @param securityEnabled whether security is enabled for the current service
     * @param internalServiceAccessPolicy internal service token validator
     */
    @Autowired
    public CustomerAccessPolicy(
            @Value("${eventcart.security.enabled:true}") boolean securityEnabled,
            InternalServiceAccessPolicy internalServiceAccessPolicy
    ) {
        this.securityEnabled = securityEnabled;
        this.internalServiceAccessPolicy = internalServiceAccessPolicy;
    }

    /**
     * Creates the customer ownership policy for focused unit tests.
     *
     * @param securityEnabled whether security is enabled for the current test
     */
    public CustomerAccessPolicy(boolean securityEnabled) {
        this(securityEnabled, null);
    }

    /**
     * Checks whether the authenticated user can access a customer-owned resource.
     *
     * @param customerId customer ID from the request or resource
     * @param authentication current authentication
     * @return {@code true} when access is allowed
     */
    public boolean canAccessCustomer(String customerId, Authentication authentication) {
        if (!securityEnabled) {
            return true;
        }
        if (internalServiceAccessPolicy != null && internalServiceAccessPolicy.isAllowedCurrentInternalRequest()) {
            return true;
        }
        if (customerId == null || customerId.isBlank() || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (hasAnyRole(authentication, "ROLE_ADMIN", "ROLE_SUPPORT")) {
            return true;
        }
        return customerIdentifiers(authentication).contains(customerId);
    }

    /**
     * Throws an access-denied exception when the current user cannot access a customer resource.
     *
     * @param customerId customer ID from the loaded resource
     * @param authentication current authentication
     */
    public void requireCustomerAccess(String customerId, Authentication authentication) {
        if (!canAccessCustomer(customerId, authentication)) {
            throw new AccessDeniedException("Authenticated user cannot access customer resource: " + customerId);
        }
    }

    /**
     * Checks whether the authentication has any of the supplied authorities.
     *
     * @param authentication current authentication
     * @param roles roles to check
     * @return {@code true} when at least one role is present
     */
    private boolean hasAnyRole(Authentication authentication, String... roles) {
        Set<String> expectedRoles = Set.of(roles);
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(expectedRoles::contains);
    }

    /**
     * Extracts customer identifiers from JWT claims and authentication name.
     *
     * @param authentication current authentication
     * @return possible customer identifiers for the current user
     */
    private Set<String> customerIdentifiers(Authentication authentication) {
        Set<String> identifiers = new HashSet<>();
        identifiers.add(authentication.getName());

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            addIfPresent(identifiers, jwt.getClaimAsString("customer_id"));
            addIfPresent(identifiers, jwt.getClaimAsString("customerId"));
            addIfPresent(identifiers, jwt.getClaimAsString("preferred_username"));
            addIfPresent(identifiers, jwt.getSubject());
        }

        identifiers.removeIf(Objects::isNull);
        identifiers.removeIf(String::isBlank);
        return identifiers;
    }

    /**
     * Adds a value to a set when it is not blank.
     *
     * @param identifiers target set
     * @param value candidate identifier
     */
    private void addIfPresent(Set<String> identifiers, String value) {
        if (value != null && !value.isBlank()) {
            identifiers.add(value);
        }
    }
}
