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
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.access.support.RestTestHelper.*;
import static org.access.support.TestFixtures.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Role Management endpoints in AccessResource.
 *
 * Tests role creation, update, deletion, and permission grant/revoke.
 */
@QuarkusTest
@DisplayName("Role Management Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleManagementIT {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    PermissionRepository permissionRepository;

    private static RoleId adminRoleId;
    private static RoleId systemRoleId;
    private static PermissionId manageRolesPermId;
    private static PermissionId roleViewPermId;
    private static PermissionId grantablePermId;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            // Create permissions
            Permission manageRoles = aPermission().withCode("MANAGE_ROLES").withName("Manage Roles").withTier(SecurityTier.TIER_1).build();
            Permission roleView = aPermission().withCode("ROLE_VIEW").withName("View Roles").withTier(SecurityTier.TIER_2).build();
            Permission grantable = aPermission().withCode("GRANTABLE_PERM").withName("Grantable Permission").withTier(SecurityTier.TIER_2).build();

            permissionRepository.save(manageRoles);
            permissionRepository.save(roleView);
            permissionRepository.save(grantable);

            manageRolesPermId = manageRoles.id();
            roleViewPermId = roleView.id();
            grantablePermId = grantable.id();

            // Create admin role with MANAGE_ROLES permission
            Role adminRole = aRole()
                .withCode("ROLE_MGMT_ADMIN")
                .withName("Role Mgmt Admin")
                .withTier(SecurityTier.TIER_1)
                .withPermissions(Set.of(manageRolesPermId, roleViewPermId))
                .build();
            roleRepository.save(adminRole);
            adminRoleId = adminRole.id();

            // Create a system role for testing system role protection
            Role sysRole = aRole()
                .withCode("ROLE_MGMT_SYSTEM")
                .withName("Role Mgmt System")
                .withTier(SecurityTier.TIER_0)
                .asSystem()
                .build();
            roleRepository.save(sysRole);
            systemRoleId = sysRole.id();

            setupComplete = true;
        }
    }

    // ==================== Create Role Tests ====================

    @Nested
    @DisplayName("Create Role (POST /roles)")
    class CreateRoleEndpoint {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should create role with MANAGE_ROLES permission")
        void shouldCreateRoleWithManageRolesPermission() {
            // Given - admin with MANAGE_ROLES permission
            User admin = aUser()
                .withEmail("admin-create-role@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            String uniqueCode = "NEW_ROLE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // When/Then - create role should succeed
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                    {
                        "code": "%s",
                        "name": "New Test Role",
                        "tier": "TIER_2"
                    }
                    """, uniqueCode))
            .when()
                .post(ACCESS_PATH + "/roles")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("code", equalTo(uniqueCode));

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should return 403 without MANAGE_ROLES permission")
        void shouldReturn403WithoutManageRolesPermission() {
            // Given - user without MANAGE_ROLES permission
            Role viewerRole = aRole()
                .withCode("ROLE_VIEWER_ONLY")
                .withName("Role Viewer Only")
                .withTier(SecurityTier.TIER_2)
                .withPermission(roleViewPermId)
                .build();
            roleRepository.save(viewerRole);

            User viewer = aUser()
                .withEmail("viewer-create-role@example.com")
                .withRoleId(viewerRole.id())
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("ROLE_VIEWER_ONLY")
                .build();

            // When/Then - create role should fail
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "code": "UNAUTHORIZED_ROLE",
                        "name": "Unauthorized Role",
                        "tier": "TIER_2"
                    }
                    """)
            .when()
                .post(ACCESS_PATH + "/roles")
            .then()
                .statusCode(403);

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should return 400 for duplicate role code")
        void shouldReturn400ForDuplicateCode() {
            // Given - admin with MANAGE_ROLES permission
            User admin = aUser()
                .withEmail("admin-dup-role@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - duplicate code should fail
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "code": "ROLE_MGMT_ADMIN",
                        "name": "Duplicate Role",
                        "tier": "TIER_2"
                    }
                    """)
            .when()
                .post(ACCESS_PATH + "/roles")
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }
    }

    // ==================== Update/Delete Role Tests ====================

    @Nested
    @DisplayName("Update/Delete Role (PUT/DELETE /roles/{id})")
    class UpdateDeleteRoleEndpoint {

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should update role with MANAGE_ROLES permission")
        void shouldUpdateRoleWithManageRolesPermission() {
            // Given - admin and target role
            User admin = aUser()
                .withEmail("admin-update-role@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            Role targetRole = aRole()
                .withCode("UPDATABLE_ROLE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .withName("Updatable Role")
                .withTier(SecurityTier.TIER_2)
                .build();
            roleRepository.save(targetRole);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - update should succeed
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Updated Role Name",
                        "description": "Updated description"
                    }
                    """)
            .when()
                .put(ACCESS_PATH + "/roles/" + targetRole.id().id())
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should return 400 for system role modification")
        void shouldReturn400ForSystemRoleModification() {
            // Given - admin trying to modify system role
            User admin = aUser()
                .withEmail("admin-modify-sys@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - modify system role should fail
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Hacked System Role"
                    }
                    """)
            .when()
                .put(ACCESS_PATH + "/roles/" + systemRoleId.id())
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(6)
        @Transactional
        @DisplayName("should delete role with MANAGE_ROLES permission")
        void shouldDeleteRoleWithManageRolesPermission() {
            // Given - admin and deletable role
            User admin = aUser()
                .withEmail("admin-delete-role@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            Role deletableRole = aRole()
                .withCode("DELETABLE_ROLE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .withName("Deletable Role")
                .withTier(SecurityTier.TIER_2)
                .build();
            roleRepository.save(deletableRole);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - delete should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete(ACCESS_PATH + "/roles/" + deletableRole.id().id())
            .then()
                .statusCode(204);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(7)
        @Transactional
        @DisplayName("should return 400 for system role deletion")
        void shouldReturn400ForSystemRoleDeletion() {
            // Given - admin trying to delete system role
            User admin = aUser()
                .withEmail("admin-delete-sys@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - delete system role should fail
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete(ACCESS_PATH + "/roles/" + systemRoleId.id())
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }
    }

    // ==================== Grant/Revoke Permission Tests ====================

    @Nested
    @DisplayName("Grant/Revoke Permission (PUT/DELETE /roles/{id}/permissions/{permId})")
    class GrantRevokePermissionEndpoint {

        @Test
        @Order(8)
        @Transactional
        @DisplayName("should grant permission with MANAGE_ROLES permission")
        void shouldGrantPermissionWithManageRolesPermission() {
            // Given - admin and target role
            User admin = aUser()
                .withEmail("admin-grant-perm@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            Role targetRole = aRole()
                .withCode("GRANT_TARGET_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .withName("Grant Target Role")
                .withTier(SecurityTier.TIER_2)
                .build();
            roleRepository.save(targetRole);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - grant permission should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .put(ACCESS_PATH + "/roles/" + targetRole.id().id() + "/permissions/" + grantablePermId.id())
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(9)
        @Transactional
        @DisplayName("should revoke permission with MANAGE_ROLES permission")
        void shouldRevokePermissionWithManageRolesPermission() {
            // Given - admin and role with permission
            User admin = aUser()
                .withEmail("admin-revoke-perm@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            Role targetRole = aRole()
                .withCode("REVOKE_TARGET_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .withName("Revoke Target Role")
                .withTier(SecurityTier.TIER_2)
                .withPermission(grantablePermId)
                .build();
            roleRepository.save(targetRole);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - revoke permission should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete(ACCESS_PATH + "/roles/" + targetRole.id().id() + "/permissions/" + grantablePermId.id())
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }
    }

    // ==================== Query Role Tests ====================

    @Nested
    @DisplayName("Query Roles")
    class QueryRolesEndpoint {

        @Test
        @Order(10)
        @Transactional
        @DisplayName("should return all roles with ROLE_VIEW permission")
        void shouldReturnAllRolesWithRoleViewPermission() {
            // Given - user with ROLE_VIEW permission
            User viewer = aUser()
                .withEmail("viewer-roles@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - query should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/roles")
            .then()
                .statusCode(200)
                .body("$", not(empty()));

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }

        @Test
        @Order(11)
        @Transactional
        @DisplayName("should return role by ID with ROLE_VIEW permission")
        void shouldReturnRoleByIdWithRoleViewPermission() {
            // Given - user with ROLE_VIEW permission
            User viewer = aUser()
                .withEmail("viewer-role-byid@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("ROLE_MGMT_ADMIN")
                .build();

            // When/Then - query should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/roles/" + adminRoleId.id())
            .then()
                .statusCode(200)
                .body("id", equalTo(adminRoleId.id().toString()));

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }
    }
}
