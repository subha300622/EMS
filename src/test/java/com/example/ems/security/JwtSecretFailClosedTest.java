package com.example.ems.security;

import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regression test: verifies the application is fail-closed with respect
 * to JWT secret configuration.
 *
 * <p>
 * If JWT_SECRET is absent, blank, or too short (&lt;32 bytes), the
 * {@link JwtService#validateSecret()} {@code @PostConstruct} method must
 * throw {@link IllegalStateException}, preventing the context from starting.
 *
 * <p>
 * This test guards against:
 * <ul>
 * <li>Re-introduction of a hardcoded fallback in {@code application.yml}
 * (e.g. {@code ${JWT_SECRET:some-default}})</li>
 * <li>Removal of the {@code @PostConstruct} validation in
 * {@link JwtService}</li>
 * <li>Weakening of the minimum-length check below 32 bytes</li>
 * </ul>
 */
@ActiveProfiles("test")
class JwtSecretFailClosedTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Direct unit-test of the @PostConstruct guard — does not need a full
     * Spring context, just the JwtService bean with a controlled property.
     */
    private JwtService buildServiceWithSecret(String secret) throws Exception {
        JwtService svc = new JwtService();
        // Inject the field via reflection (mirrors what Spring @Value does)
        var field = JwtService.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(svc, secret);
        svc.validateSecret(); // @PostConstruct
        return svc;
    }

    // -------------------------------------------------------------------------
    // 1. Blank / null secrets must be rejected at startup
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("validateSecret rejects null JWT secret → IllegalStateException")
    void validateSecret_null_throwsIllegalStateException() throws Exception {
        JwtService svc = new JwtService();
        var field = JwtService.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(svc, null);

        assertThatThrownBy(svc::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("validateSecret rejects blank JWT secret → IllegalStateException")
    void validateSecret_blank_throwsIllegalStateException() throws Exception {
        JwtService svc = new JwtService();
        var field = JwtService.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(svc, "   ");

        assertThatThrownBy(svc::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    // -------------------------------------------------------------------------
    // 2. Secrets below the 32-byte minimum must be rejected
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("validateSecret rejects a secret shorter than 32 bytes → IllegalStateException")
    void validateSecret_tooShort_throwsIllegalStateException() throws Exception {
        JwtService svc = new JwtService();
        var field = JwtService.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(svc, "short-secret"); // 12 chars < 32 bytes

        assertThatThrownBy(svc::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    @DisplayName("validateSecret rejects a 31-byte secret (boundary) → IllegalStateException")
    void validateSecret_31bytes_throwsIllegalStateException() throws Exception {
        String secret31 = "a".repeat(31); // exactly 31 bytes in UTF-8
        JwtService svc = new JwtService();
        var field = JwtService.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(svc, secret31);

        assertThatThrownBy(svc::validateSecret)
                .isInstanceOf(IllegalStateException.class);
    }

    // -------------------------------------------------------------------------
    // 3. Valid secrets (≥32 bytes) must be accepted
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("validateSecret accepts a 32-byte secret (minimum boundary) → no exception")
    void validateSecret_32bytes_accepted() throws Exception {
        String secret32 = "a".repeat(32);
        assertThat(buildServiceWithSecret(secret32)).isNotNull();
    }

    @Test
    @DisplayName("validateSecret accepts a 64-byte Base64 secret → no exception")
    void validateSecret_64bytesBase64_accepted() throws Exception {
        // Simulates the output of: openssl rand -base64 64
        String secret = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAA=="; // 88 chars, 64 bytes decoded, > 32 bytes as-is
        assertThat(buildServiceWithSecret(secret)).isNotNull();
    }

    // -------------------------------------------------------------------------
    // 4. Detect hardcoded fallbacks in application.yml
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("JWT_SECRET fallback in test config must not look like a real secret")
    void testConfig_jwtFallback_mustBeClearlyFakeTestValue() throws Exception {
        // The test application.yml is allowed to have a fallback for JWT_SECRET so
        // that local development and CI without the env var still works.
        // However the fallback value MUST:
        // (a) never be the old committed value (which may have been leaked), AND
        // (b) be clearly labelled as a test-only placeholder (contain "test" or
        // "dummy")
        //
        // This prevents a developer from accidentally committing a real-looking secret.
        try (var stream = getClass().getResourceAsStream("/application.yml")) {
            if (stream == null)
                return;
            String contents = new String(stream.readAllBytes());

            // If there is a JWT_SECRET fallback in the test config, validate it
            var matcher = java.util.regex.Pattern
                    .compile("\\$\\{JWT_SECRET:([^}]+)\\}")
                    .matcher(contents);
            if (matcher.find()) {
                String fallbackValue = matcher.group(1).toLowerCase();
                assertThat(fallbackValue)
                        .as("JWT_SECRET fallback in test application.yml must be clearly " +
                                "labelled as test-only (must contain 'test', 'dummy', or 'fake'). " +
                                "Never use a real-looking secret as a default.")
                        .containsAnyOf("test", "dummy", "fake");
            }
        }
    }

    @Test
    @DisplayName("test application.yml fallback must not reuse the old committed secret value")
    void testApplicationYml_fallbackNotOldCommittedSecret() throws Exception {
        // Ensures the old compromised default value was rotated.
        final String oldCommittedSecret = "jwtSecretKeyForEmsBackendDevelopmentShouldBeLongAndSecure32Bytes!";

        try (var stream = getClass().getResourceAsStream("/application.yml")) {
            if (stream == null)
                return;
            String contents = new String(stream.readAllBytes());
            assertThat(contents)
                    .as("The old committed JWT secret value must have been rotated. " +
                            "Update the test fallback in src/test/resources/application.yml.")
                    .doesNotContain(oldCommittedSecret);
        }
    }
}
