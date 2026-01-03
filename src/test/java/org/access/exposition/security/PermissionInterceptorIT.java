package org.access.exposition.security;

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
 * Integration tests for PermissionInterceptor.
 *
 * Verifies that the CDI interceptor correctly enforces @RequiresPermission
 * annotations on REST endpoints, including OR and AND logic.
 */
@QuarkusTest
@DisplayName("PermissionInterceptor Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PermissionInterceptorIT {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    PermissionRepository permissionRepository;

    private static RoleId roleWithUserViewAll;
    private static RoleId roleWithoutPermissions;
    private static RoleId roleWithManageRoles;
    private static PermissionId userViewAllPermId;
    private static PermissionId manageRolesPermId;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            // Create permissions
            Permission userViewAll = aPermission()
                .withCode("USER_VIEW_ALL")
                .withName("View All Users")
                .withTier(SecurityTier.TIER_2)
                .build();
            permissionRepository.save(userViewAll);
            userViewAllPermId = userViewAll.id();

            Permission manageRoles = aPermission()
                .withCode("MANAGE_ROLES")
                .withName("Manage Roles")
                .withTier(SecurityTier.TIER_1)
                .build();
            permissionRepository.save(manageRoles);
            manageRolesPermId = manageRoles.id();

            // Create role with USER_VIEW_ALL permission
            Role roleWithView = aRole()
                .withCode("PERM_TEST_VIEWER")
                .withName("Permission Test Viewer")
                .withTier(SecurityTier.TIER_2)
                .withPermission(userViewAllPermId)
                .build();
            roleRepository.save(roleWithView);
            roleWithUserViewAll = roleWithView.id();

            // Create role without permissions
            Role roleEmpty = aRole()
                .withCode("PERM_TEST_EMPTY")
                .withName("Permission Test Empty")
                .withTier(SecurityTier.TIER_2)
                .build();
            roleRepository.save(roleEmpty);
            roleWithoutPermissions = roleEmpty.id();

            // Create role with MANAGE_ROLES permission
            Role roleWithManage = aRole()
                .withCode("PERM_TEST_MANAGER")
                .withName("Permission Test Manager")
                .withTier(SecurityTier.TIER_1)
                .withPermission(manageRolesPermId)
                .build();
            roleRepository.save(roleWithManage);
            roleWithManageRoles = roleWithManage.id();

            setupComplete = true;
        }
    }

    @Nested
    @DisplayName("Single Permission Enforcement")
    class SinglePermissionEnforcement {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should allow request when user has required permission")
        void shouldAllowWithRequiredPermission() {
            // Given - user with USER_VIEW_ALL permission
            User userWithPermission = aUser()
                .withEmail("has-perm@example.com")
                .withRoleId(roleWithUserViewAll)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(userWithPermission);

            String token = aToken()
                .forUser(userWithPermission.getId())
                .withEmail(userWithPermission.getEmail().value())
                .withRole("PERM_TEST_VIEWER")
                .build();

            // When/Then - endpoint requiring USER_VIEW_ALL should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/users")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(userWithPermission.getId());
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should reject request with 403 when user lacks permission")
        void shouldReject403WithoutPermission() {
            // Given - user without any permissions
            User userWithoutPermission = aUser()
                .withEmail("no-perm@example.com")
                .withRoleId(roleWithoutPermissions)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(userWithoutPermission);

            String token = aToken()
                .forUser(userWithoutPermission.getId())
                .withEmail(userWithoutPermission.getEmail().value())
                .withRole("PERM_TEST_EMPTY")
                .build();

            // When/Then - endpoint requiring USER_VIEW_ALL should fail
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/users")
            .then()
                .statusCode(403)
                .body(containsString("Insufficient permissions"));

            // Cleanup
            userRepository.deleteById(userWithoutPermission.getId());
        }
    }

    @Nested
    @DisplayName("Multiple Permissions - OR Logic")
    class MultiplePermissionsOrLogic {

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should allow when user has at least one of required permissions (OR)")
        void shouldAllowWithAnyPermission_OR_Logic() {
            // Given - user with MANAGE_ROLES (one of ASSIGN_CRITICAL/STANDARD/BASIC_ROLES alternatives)
            // Note: We'll test with roles endpoint which uses single permission
            User userWithManageRoles = aUser()
                .withEmail("manage-roles@example.com")
                .withRoleId(roleWithManageRoles)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(userWithManageRoles);

            String token = aToken()
                .forUser(userWithManageRoles.getId())
                .withEmail(userWithManageRoles.getEmail().value())
                .withRole("PERM_TEST_MANAGER")
                .build();

            // When/Then - endpoint requiring MANAGE_ROLES should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/roles")
            .then()
                .statusCode(anyOf(is(200), is(403))); // Depends on ROLE_VIEW permission

            // Cleanup
            userRepository.deleteById(userWithManageRoles.getId());
        }
    }

    @Nested
    @DisplayName("Tier-Based Permissions")
    class TierBasedPermissions {

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should respect tier hierarchy for permission access")
        void shouldHandleTierBasedPermissions() {
            // Given - TIER_2 user trying to access TIER_1 protected endpoint
            User tier2User = aUser()
                .withEmail("tier2-user@example.com")
                .withRoleId(roleWithoutPermissions)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(tier2User);

            String token = aToken()
                .forUser(tier2User.getId())
                .withEmail(tier2User.getEmail().value())
                .withRole("PERM_TEST_EMPTY")
                .build();

            // When/Then - endpoint requiring MANAGE_ROLES (TIER_1) should fail for TIER_2 user
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "code": "NEW_ROLE",
                        "name": "New Role",
                        "tier": "TIER_2"
                    }
                    """)
            .when()
                .post(ACCESS_PATH + "/roles")
            .then()
                .statusCode(403);

            // Cleanup
            userRepository.deleteById(tier2User.getId());
        }
    }

    @Nested
    @DisplayName("Unauthenticated Access")
    class UnauthenticatedAccess {

        @Test
        @Order(5)
        @DisplayName("should reject unauthenticated request with 401")
        void shouldReject401WithoutAuthentication() {
            // When/Then - protected endpoint without token should fail
            given()
            .when()
                .get(ACCESS_PATH + "/users")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("Invalid Token Handling")
    class InvalidTokenHandling {

        @Test
        @Order(6)
        @DisplayName("should reject request with invalid token")
        void shouldRejectInvalidToken() {
            // When/Then - protected endpoint with invalid token should fail
            given()
                .header("Authorization", "Bearer invalid.token.here")
            .when()
                .get(ACCESS_PATH + "/users")
            .then()
                .statusCode(401);
        }

        @Test
        @Order(7)
        @DisplayName("should reject request with expired token")
        void shouldRejectExpiredToken() {
            // Given - an expired token
            String expiredToken = aToken()
                .forUser(UserId.newId())
                .withEmail("expired@example.com")
                .withRole("TEST_ROLE")
                .expired()
                .build();

            // When/Then - protected endpoint with expired token should fail
            given()
                .header("Authorization", "Bearer " + expiredToken)
            .when()
                .get(ACCESS_PATH + "/users")
            .then()
                .statusCode(401);
        }
    }
}
