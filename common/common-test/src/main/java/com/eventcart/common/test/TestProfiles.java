package com.eventcart.common.test;

/**
 * Shared Spring profile names used by EventCart tests.
 *
 * <p>Keeping profile names in one place avoids subtle differences such as
 * {@code integrationTest}, {@code integration-test}, and {@code it} across
 * services.</p>
 */
public final class TestProfiles {
    /**
     * Profile name used for tests that require real infrastructure or
     * Testcontainers-backed dependencies.
     */
    public static final String INTEGRATION_TEST = "integration-test";

    /**
     * Prevents creation of this constants-only utility class.
     */
    private TestProfiles() {
    }
}
