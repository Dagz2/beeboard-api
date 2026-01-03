package org.access.exposition;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.domain.model.*;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.access.support.RestTestHelper.*;
import static org.access.support.TestFixtures.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Query endpoints in AccessResource.
 *
 * Tests the /me endpoint and various query operations.
 */
@QuarkusTest
@DisplayName("Query Endpoints Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QueryEndpointsIT {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    PermissionRepository permissionRepository;

    private static RoleId testRoleId;
    private static PermissionId userViewAllPermId;
    private static PermissionId roleViewPermId;
    private static PermissionId permViewPermId;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            // Create permissions
            Permission userViewAll = aPermission()
                .withCode("QUERY_USER_VIEW_ALL")
                .withName("Query View All Users")
                .withTier(SecurityTier.TIER_2)
                .build();
            permissionRepository.save(userViewAll);
            userViewAllPermId = userViewAll.id();

            Permission roleView = aPermission()
                .withCode("QUERY_ROLE_VIEW")
                .withName("Query View Roles")
                .withTier(SecurityTier.TIER_2)
                .build();
            permissionRepository.save(roleView);
            roleViewPermId = roleView.id();

            Permission permView = aPermission()
                .withCode("QUERY_PERMISSION_VIEW")
                .withName("Query View Permissions")
                .withTier(SecurityTier.TIER_2)
                .build();
            permissionRepository.save(permView);
            permViewPermId = permView.id();

            // Create test role with all view permissions
            Role testRole = aRole()
                .withCode("QUERY_TEST_ROLE")
                .withName("Query Test Role")
                .withTier(SecurityTier.TIER_2)
                .withPermissions(Set.of(userViewAllPermId, roleViewPermId, permViewPermId))
                .build();
            roleRepository.save(testRole);
            testRoleId = testRole.id();

            setupComplete = true;
        }
    }

    // ==================== /me Endpoint Tests ====================

    @Nested
    @DisplayName("Current User Endpoint (GET /me)")
    class CurrentUserEndpoint {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should return current user profile")
        void shouldReturnCurrentUserProfile() {
            // Given - an authenticated user
            User user = aUser()
                .withEmail("me-profile@example.com")
                .withFullName("Current", "User")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            String token = aToken()
                .forUser(user.getId())
                .withEmail(user.getEmail().value())
                .withRole("QUERY_TEST_ROLE")
                .build();

            // When/Then - should return profile
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/me")
            .then()
                .statusCode(200)
                .body("userId", equalTo(user.getId().id().toString()))
                .body("email", equalTo("me-profile@example.com"))
                .body("fullName", containsString("Current"));

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should return permissions list in profile")
        void shouldReturnPermissionsList() {
            // Given - an authenticated user with permissions
            User user = aUser()
                .withEmail("me-perms@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            String token = aToken()
                .forUser(user.getId())
                .withEmail(user.getEmail().value())
                .withRole("QUERY_TEST_ROLE")
                .build();

            // When/Then - should return permissions
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/me")
            .then()
                .statusCode(200)
                .body("permissions", not(empty()));

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should return tier information in profile")
        void shouldReturnTierInformation() {
            // Given - an authenticated user
            User user = aUser()
                .withEmail("me-tier@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            String token = aToken()
                .forUser(user.getId())
                .withEmail(user.getEmail().value())
                .withRole("QUERY_TEST_ROLE")
                .build();

            // When/Then - should return tier
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/me")
            .then()
                .statusCode(200)
                .body("tier", notNullValue());

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(4)
        @DisplayName("should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() {
            // When/Then - should reject unauthenticated request
            given()
            .when()
                .get(ACCESS_PATH + "/me")
            .then()
                .statusCode(401);
        }
    }

    // ==================== Users by Role Tests ====================

    @Nested
    @DisplayName("Users by Role (GET /users/role/{roleId})")
    class UsersByRoleEndpoint {

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should return users by role with USER_VIEW_ALL permission")
        void shouldReturnUsersByRoleWithUserViewAllPermission() {
            // Given - viewer and users with the same role
            User viewer = aUser()
                .withEmail("viewer-byrole@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            User user1 = aUser()
                .withEmail("user1-byrole@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user1);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("QUERY_TEST_ROLE")
                .build();

            // When/Then - should return users
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/users/role/" + testRoleId.id())
            .then()
                .statusCode(200)
                .body("$", not(empty()));

            // Cleanup
            userRepository.deleteById(viewer.getId());
            userRepository.deleteById(user1.getId());
        }
    }

    // ==================== Non-Existent Resource Tests ====================

    @Nested
    @DisplayName("Non-Existent Resources")
    class NonExistentResources {

        @Test
        @Order(6)
        @Transactional
        @DisplayName("should return 404 for non-existent user")
        void shouldReturn404ForNonExistentUser() {
            // Given - a viewer
            User viewer = aUser()
                .withEmail("viewer-404user@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("QUERY_TEST_ROLE")
                .build();

            // When/Then - should return 404
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/users/" + UserId.newId().id())
            .then()
                .statusCode(404);

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }

        @Test
        @Order(7)
        @Transactional
        @DisplayName("should return 404 for non-existent role")
        void shouldReturn404ForNonExistentRole() {
            // Given - a viewer
            User viewer = aUser()
                .withEmail("viewer-404role@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("QUERY_TEST_ROLE")
                .build();

            // When/Then - should return 404
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/roles/" + RoleId.newId().id())
            .then()
                .statusCode(404);

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }

        @Test
        @Order(8)
        @Transactional
        @DisplayName("should return 404 for non-existent permission")
        void shouldReturn404ForNonExistentPermission() {
            // Given - a viewer
            User viewer = aUser()
                .withEmail("viewer-404perm@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("QUERY_TEST_ROLE")
                .build();

            // When/Then - should return 404
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/permissions/" + PermissionId.newId().id())
            .then()
                .statusCode(404);

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }
    }

    // ==================== Invalid UUID Tests ====================

    @Nested
    @DisplayName("Invalid UUID Handling")
    class InvalidUuidHandling {

        @Test
        @Order(9)
        @Transactional
        @DisplayName("should return 400 for invalid UUID format")
        void shouldReturn400ForInvalidUuidFormat() {
            // Given - a viewer
            User viewer = aUser()
                .withEmail("viewer-invaliduuid@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("QUERY_TEST_ROLE")
                .build();

            // When/Then - should return 400 or 404
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/users/not-a-uuid")
            .then()
                .statusCode(anyOf(is(400), is(404)));

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }
    }
}
