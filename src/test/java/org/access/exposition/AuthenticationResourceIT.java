package org.access.exposition;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.domain.model.*;
import org.access.domain.repository.PasswordResetRequestRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.access.support.RestTestHelper.*;
import static org.access.support.TestFixtures.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for AuthenticationResource.
 *
 * Tests the login, password reset request, and password reset completion endpoints.
 */
@QuarkusTest
@DisplayName("AuthenticationResource Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticationResourceIT {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    PasswordResetRequestRepository passwordResetRequestRepository;

    private static RoleId testRoleId;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            Role role = aRole()
                .withCode("AUTH_TEST_ROLE")
                .withName("Auth Test Role")
                .withTier(SecurityTier.TIER_2)
                .build();
            roleRepository.save(role);
            testRoleId = role.id();
            setupComplete = true;
        }
    }

    // ==================== Login Tests ====================

    @Nested
    @DisplayName("Login Endpoint")
    class LoginEndpoint {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should return JWT token for valid credentials")
        void shouldReturnJwtTokenForValidCredentials() {
            // Given - a valid user
            String password = "SecurePassword123!@#$%^&*()_+abcd";
            User user = aUser()
                .withEmail("login-valid@example.com")
                .withPassword(Password.fromPlainText(password))
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            // When/Then - login should succeed with JWT
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }
                    """, user.getEmail().value(), password))
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", greaterThan(0));

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should return 401 for invalid password")
        void shouldReturn401ForInvalidPassword() {
            // Given - a valid user
            User user = aUser()
                .withEmail("login-invalid-pwd@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            // When/Then - login with wrong password should fail
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "email": "%s",
                        "password": "WrongPassword123!@#$%^&*()_+abcd"
                    }
                    """, user.getEmail().value()))
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(401);

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @DisplayName("should return 401 for non-existent user")
        void shouldReturn401ForNonExistentUser() {
            // When/Then - login with unknown email should fail
            given()
                .contentType("application/json")
                .body("""
                    {
                        "email": "nonexistent-user@example.com",
                        "password": "AnyPassword123!@#$%^&*()_+abcd"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(401);
        }

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should return 401 for locked account")
        void shouldReturn401ForLockedAccount() {
            // Given - a locked user
            String password = "SecurePassword123!@#$%^&*()_+abcd";
            User lockedUser = aUser()
                .withEmail("login-locked@example.com")
                .withPassword(Password.fromPlainText(password))
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .locked()
                .build();
            userRepository.save(lockedUser);

            // When/Then - login should fail for locked account
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }
                    """, lockedUser.getEmail().value(), password))
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(401);

            // Cleanup
            userRepository.deleteById(lockedUser.getId());
        }

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should return 401 for expired contract")
        void shouldReturn401ForExpiredContract() {
            // Given - a user with expired contract
            String password = "SecurePassword123!@#$%^&*()_+abcd";
            User expiredUser = aUser()
                .withEmail("login-expired@example.com")
                .withPassword(Password.fromPlainText(password))
                .withRoleId(testRoleId)
                .withExpiredContract()
                .unlocked()
                .build();
            userRepository.save(expiredUser);

            // When/Then - login should fail for expired contract
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }
                    """, expiredUser.getEmail().value(), password))
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(401);

            // Cleanup
            userRepository.deleteById(expiredUser.getId());
        }

        @Test
        @Order(6)
        @DisplayName("should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmailFormat() {
            // When/Then - login with invalid email should fail validation
            given()
                .contentType("application/json")
                .body("""
                    {
                        "email": "invalid-email",
                        "password": "AnyPassword123!@#$%^&*()_+abcd"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(400);
        }
    }

    // ==================== Password Reset Request Tests ====================

    @Nested
    @DisplayName("Password Reset Request Endpoint")
    class PasswordResetRequestEndpoint {

        @Test
        @Order(7)
        @Transactional
        @DisplayName("should return 200 for existing user (silent success)")
        void shouldReturn200ForExistingUser() {
            // Given - a valid user
            User user = aUser()
                .withEmail("reset-existing@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            // When/Then - should return 200
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "email": "%s"
                    }
                    """, user.getEmail().value()))
            .when()
                .post(AUTH_PATH + "/password-reset/request")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(8)
        @DisplayName("should return 200 for non-existent user (enumeration prevention)")
        void shouldReturn200ForNonExistentUser() {
            // When/Then - should return 200 even for unknown email
            given()
                .contentType("application/json")
                .body("""
                    {
                        "email": "nonexistent-reset@example.com"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/password-reset/request")
            .then()
                .statusCode(200);
        }

        @Test
        @Order(9)
        @Transactional
        @DisplayName("should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimitExceeded() {
            // Given - a user
            User user = aUser()
                .withEmail("rate-limited@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            String body = String.format("""
                {
                    "email": "%s"
                }
                """, user.getEmail().value());

            // When - make 4 requests (limit is 3/hour)
            for (int i = 0; i < 3; i++) {
                given()
                    .contentType("application/json")
                    .body(body)
                .when()
                    .post(AUTH_PATH + "/password-reset/request")
                .then()
                    .statusCode(200);
            }

            // Then - 4th request should be rate limited
            given()
                .contentType("application/json")
                .body(body)
            .when()
                .post(AUTH_PATH + "/password-reset/request")
            .then()
                .statusCode(429);

            // Cleanup
            userRepository.deleteById(user.getId());
        }
    }

    // ==================== Password Reset Complete Tests ====================

    @Nested
    @DisplayName("Password Reset Complete Endpoint")
    class PasswordResetCompleteEndpoint {

        @Test
        @Order(10)
        @Transactional
        @DisplayName("should return 200 with valid token")
        void shouldReturn200WithValidToken() {
            // Given - a user with a valid reset token
            User user = aUser()
                .withEmail("reset-complete@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            ResetToken token = ResetToken.generate();
            PasswordResetRequest resetRequest = aPasswordResetRequest()
                .withUserId(user.getId())
                .withToken(token)
                .withExpiresAt(Instant.now().plusSeconds(900)) // 15 minutes
                .build();
            passwordResetRequestRepository.save(resetRequest);

            // When/Then - complete reset should succeed
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "token": "%s",
                        "newPassword": "NewSecurePassword123!@#$%^&*()_+xyz"
                    }
                    """, token.value()))
            .when()
                .post(AUTH_PATH + "/password-reset/complete")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(11)
        @Transactional
        @DisplayName("should return 400 for expired token")
        void shouldReturn400ForExpiredToken() {
            // Given - a user with an expired reset token
            User user = aUser()
                .withEmail("reset-expired@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            ResetToken token = ResetToken.generate();
            PasswordResetRequest expiredRequest = aPasswordResetRequest()
                .withUserId(user.getId())
                .withToken(token)
                .expired()
                .build();
            passwordResetRequestRepository.save(expiredRequest);

            // When/Then - complete reset should fail
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "token": "%s",
                        "newPassword": "NewSecurePassword123!@#$%^&*()_+xyz"
                    }
                    """, token.value()))
            .when()
                .post(AUTH_PATH + "/password-reset/complete")
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(12)
        @Transactional
        @DisplayName("should return 400 for already used token")
        void shouldReturn400ForAlreadyUsedToken() {
            // Given - a user with a used reset token
            User user = aUser()
                .withEmail("reset-used@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            ResetToken token = ResetToken.generate();
            PasswordResetRequest usedRequest = aPasswordResetRequest()
                .withUserId(user.getId())
                .withToken(token)
                .withExpiresAt(Instant.now().plusSeconds(900))
                .used()
                .build();
            passwordResetRequestRepository.save(usedRequest);

            // When/Then - complete reset should fail
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "token": "%s",
                        "newPassword": "NewSecurePassword123!@#$%^&*()_+xyz"
                    }
                    """, token.value()))
            .when()
                .post(AUTH_PATH + "/password-reset/complete")
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(13)
        @DisplayName("should return 400 for invalid token format")
        void shouldReturn400ForInvalidTokenFormat() {
            // When/Then - invalid token should fail
            given()
                .contentType("application/json")
                .body("""
                    {
                        "token": "invalid-token",
                        "newPassword": "NewSecurePassword123!@#$%^&*()_+xyz"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/password-reset/complete")
            .then()
                .statusCode(400);
        }

        @Test
        @Order(14)
        @Transactional
        @DisplayName("should return 400 for weak password")
        void shouldReturn400ForWeakPassword() {
            // Given - a user with a valid reset token
            User user = aUser()
                .withEmail("reset-weak-pwd@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            ResetToken token = ResetToken.generate();
            PasswordResetRequest resetRequest = aPasswordResetRequest()
                .withUserId(user.getId())
                .withToken(token)
                .withExpiresAt(Instant.now().plusSeconds(900))
                .build();
            passwordResetRequestRepository.save(resetRequest);

            // When/Then - weak password should fail validation
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "token": "%s",
                        "newPassword": "weak"
                    }
                    """, token.value()))
            .when()
                .post(AUTH_PATH + "/password-reset/complete")
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(15)
        @DisplayName("should return 400 for non-existent token")
        void shouldReturn400ForNonExistentToken() {
            // When/Then - non-existent token should fail
            given()
                .contentType("application/json")
                .body("""
                    {
                        "token": "a1b2c3d4e5f6789012345678901234567890123456789012345678901234",
                        "newPassword": "NewSecurePassword123!@#$%^&*()_+xyz"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/password-reset/complete")
            .then()
                .statusCode(400);
        }
    }

    // ==================== Input Validation Tests ====================

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @Order(16)
        @DisplayName("should return 400 for missing email in login")
        void shouldReturn400ForMissingEmail() {
            given()
                .contentType("application/json")
                .body("""
                    {
                        "password": "SomePassword123!@#$%^&*()_+abcd"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(400);
        }

        @Test
        @Order(17)
        @DisplayName("should return 400 for missing password in login")
        void shouldReturn400ForMissingPassword() {
            given()
                .contentType("application/json")
                .body("""
                    {
                        "email": "test@example.com"
                    }
                    """)
            .when()
                .post(AUTH_PATH + "/login")
            .then()
                .statusCode(400);
        }
    }
}
