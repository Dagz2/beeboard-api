package org.access.exposition.filter;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.domain.model.*;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.access.support.RestTestHelper.*;
import static org.access.support.TestFixtures.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for UserStatusFilter.
 *
 * Verifies that the JAX-RS filter correctly validates user status on every request,
 * rejecting locked accounts and expired contracts even with valid JWT tokens.
 */
@QuarkusTest
@DisplayName("UserStatusFilter Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserStatusFilterIT {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    private static RoleId testRoleId;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            Role role = aRole()
                .withCode("FILTER_TEST_ROLE")
                .withName("Filter Test Role")
                .withTier(SecurityTier.TIER_2)
                .build();
            roleRepository.save(role);
            testRoleId = role.id();
            setupComplete = true;
        }
    }

    @Nested
    @DisplayName("Active User Access")
    class ActiveUserAccess {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should allow request for active user with valid contract")
        void shouldAllowRequestForActiveUser() {
            // Given - an active user with valid contract
            User activeUser = aUser()
                .withEmail("active-filter@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(activeUser);

            String token = aToken()
                .forUser(activeUser.getId())
                .withEmail(activeUser.getEmail().value())
                .withRole("FILTER_TEST_ROLE")
                .build();

            // When/Then - request should succeed (200 OK for /me endpoint)
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/me")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(activeUser.getId());
        }
    }

    @Nested
    @DisplayName("Locked Account Rejection")
    class LockedAccountRejection {

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should reject request with 403 for locked user")
        void shouldReject403ForLockedUser() {
            // Given - a locked user
            User lockedUser = aUser()
                .withEmail("locked-filter@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .locked()
                .build();
            userRepository.save(lockedUser);

            String token = aToken()
                .forUser(lockedUser.getId())
                .withEmail(lockedUser.getEmail().value())
                .withRole("FILTER_TEST_ROLE")
                .build();

            // When/Then - request should be rejected with 403
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/me")
            .then()
                .statusCode(403)
                .body("message", containsString("locked"));

            // Cleanup
            userRepository.deleteById(lockedUser.getId());
        }
    }

    @Nested
    @DisplayName("Expired Contract Rejection")
    class ExpiredContractRejection {

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should reject request with 403 for expired contract")
        void shouldReject403ForExpiredContract() {
            // Given - a user with expired contract
            User expiredUser = aUser()
                .withEmail("expired-filter@example.com")
                .withRoleId(testRoleId)
                .withExpiredContract()
                .unlocked()
                .build();
            userRepository.save(expiredUser);

            String token = aToken()
                .forUser(expiredUser.getId())
                .withEmail(expiredUser.getEmail().value())
                .withRole("FILTER_TEST_ROLE")
                .build();

            // When/Then - request should be rejected with 403
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/me")
            .then()
                .statusCode(403)
                .body("message", containsString("expired"));

            // Cleanup
            userRepository.deleteById(expiredUser.getId());
        }
    }

    @Nested
    @DisplayName("Public Endpoint Bypass")
    class PublicEndpointBypass {

        @Test
        @Order(4)
        @DisplayName("should allow unauthenticated access to login endpoint")
        void shouldBypassFilterForPublicEndpoints() {
            // When/Then - login endpoint should be accessible without token
            given()
                .contentType("application/json")
                .body("""
                    {
                        "email": "nonexistent@example.com",
                        "password": "anypassword"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(anyOf(is(401), is(400))); // Not 403 from filter
        }

        @Test
        @Order(5)
        @DisplayName("should allow unauthenticated access to password reset request")
        void shouldBypassFilterForPasswordResetRequest() {
            // When/Then - password reset request should be accessible
            given()
                .contentType("application/json")
                .body("""
                    {
                        "email": "test@example.com"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/password-reset/request")
            .then()
                .statusCode(200); // Always 200 for enumeration prevention
        }
    }

    @Nested
    @DisplayName("Non-Existent User Handling")
    class NonExistentUserHandling {

        @Test
        @Order(6)
        @DisplayName("should reject request with 403 for deleted user")
        void shouldHandleNonExistentUser() {
            // Given - a token for a non-existent user
            UUID nonExistentUserId = UUID.randomUUID();
            String token = aToken()
                .forUser(nonExistentUserId)
                .withEmail("deleted@example.com")
                .withRole("FILTER_TEST_ROLE")
                .build();

            // When/Then - request should be rejected with 403
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/me")
            .then()
                .statusCode(403)
                .body("message", containsString("not found"));
        }
    }
}
