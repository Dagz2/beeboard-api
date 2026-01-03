package org.access.support;

import io.smallrye.jwt.build.Jwt;
import org.access.domain.model.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Helper class for REST integration tests.
 * Provides JWT token generation and common test utilities.
 */
public final class RestTestHelper {

    public static final String API_BASE = "/api/v1";
    public static final String AUTH_PATH = API_BASE + "/auth";
    public static final String ACCESS_PATH = API_BASE + "/access";
    public static final String ISSUER = "beeboard-test";

    private RestTestHelper() {
        // Utility class
    }

    // ==================== JWT Token Generation ====================

    /**
     * Generates a valid JWT token for testing.
     */
    public static String generateToken(UUID userId, String email, String roleCode) {
        return generateToken(userId, email, roleCode, 3600);
    }

    /**
     * Generates a JWT token with custom expiration.
     */
    public static String generateToken(UUID userId, String email, String roleCode, long durationSeconds) {
        Instant now = Instant.now();
        return Jwt.issuer(ISSUER)
                .upn(email)
                .subject(userId.toString())
                .groups(Set.of(roleCode))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(durationSeconds))
                .claim("fullName", "Test User")
                .claim("roleName", roleCode)
                .sign();
    }

    /**
     * Generates an expired JWT token for testing.
     */
    public static String generateExpiredToken(UUID userId, String email, String roleCode) {
        Instant now = Instant.now();
        Instant expired = now.minusSeconds(3600);
        return Jwt.issuer(ISSUER)
                .upn(email)
                .subject(userId.toString())
                .groups(Set.of(roleCode))
                .issuedAt(expired.minusSeconds(7200))
                .expiresAt(expired)
                .claim("fullName", "Test User")
                .claim("roleName", roleCode)
                .sign();
    }

    /**
     * Generates a token for a super admin user.
     */
    public static String generateSuperAdminToken(UUID userId, String email) {
        return generateToken(userId, email, "SUPER_ADMIN");
    }

    /**
     * Generates a token for a director user.
     */
    public static String generateDirectorToken(UUID userId, String email) {
        return generateToken(userId, email, "DIRECTOR");
    }

    /**
     * Generates a token for an HR manager user.
     */
    public static String generateHrManagerToken(UUID userId, String email) {
        return generateToken(userId, email, "HR_MANAGER");
    }

    // ==================== Token Builder ====================

    public static TokenBuilder aToken() {
        return new TokenBuilder();
    }

    public static class TokenBuilder {
        private UUID userId = UUID.randomUUID();
        private String email = "test@example.com";
        private String roleCode = "TEST_ROLE";
        private long durationSeconds = 3600;
        private String issuer = ISSUER;

        public TokenBuilder forUser(UUID userId) {
            this.userId = userId;
            return this;
        }

        public TokenBuilder forUser(UserId userId) {
            this.userId = userId.id();
            return this;
        }

        public TokenBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public TokenBuilder withRole(String roleCode) {
            this.roleCode = roleCode;
            return this;
        }

        public TokenBuilder asSuperAdmin() {
            this.roleCode = "SUPER_ADMIN";
            return this;
        }

        public TokenBuilder asDirector() {
            this.roleCode = "DIRECTOR";
            return this;
        }

        public TokenBuilder asHrManager() {
            this.roleCode = "HR_MANAGER";
            return this;
        }

        public TokenBuilder expiresIn(long seconds) {
            this.durationSeconds = seconds;
            return this;
        }

        public TokenBuilder expired() {
            this.durationSeconds = -3600;
            return this;
        }

        public String build() {
            Instant now = Instant.now();
            Instant expiry = now.plusSeconds(durationSeconds);
            Instant issued = durationSeconds < 0 ? now.plusSeconds(durationSeconds - 3600) : now;

            return Jwt.issuer(issuer)
                    .upn(email)
                    .subject(userId.toString())
                    .groups(Set.of(roleCode))
                    .issuedAt(issued)
                    .expiresAt(expiry)
                    .claim("fullName", "Test User")
                    .claim("roleName", roleCode)
                    .sign();
        }
    }

    // ==================== Test Data Setup Helpers ====================

    /**
     * Creates a test user with roles set up for integration tests.
     */
    public static TestUserData createTestUser(SecurityTier tier) {
        UUID userId = UUID.randomUUID();
        String email = "test-" + userId.toString().substring(0, 8) + "@example.com";
        String roleCode = switch (tier) {
            case TIER_0 -> "SUPER_ADMIN";
            case TIER_1 -> "DIRECTOR";
            case TIER_2 -> "HR_MANAGER";
        };

        String token = generateToken(userId, email, roleCode);
        return new TestUserData(userId, email, roleCode, tier, token);
    }

    public record TestUserData(
        UUID userId,
        String email,
        String roleCode,
        SecurityTier tier,
        String token
    ) {
        public String authHeader() {
            return "Bearer " + token;
        }
    }
}
