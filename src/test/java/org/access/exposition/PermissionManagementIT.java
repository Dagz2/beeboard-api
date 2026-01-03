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
 * Integration tests for Permission Management endpoints in AccessResource.
 *
 * Tests permission creation, update, and deletion.
 */
@QuarkusTest
@DisplayName("Permission Management Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PermissionManagementIT {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    PermissionRepository permissionRepository;

    private static RoleId adminRoleId;
    private static PermissionId managePermsPermId;
    private static PermissionId permViewPermId;
    private static PermissionId systemPermId;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            // Create permissions
            Permission managePerms = aPermission()
                .withCode("MANAGE_PERMISSIONS")
                .withName("Manage Permissions")
                .withTier(SecurityTier.TIER_0)
                .build();
            permissionRepository.save(managePerms);
            managePermsPermId = managePerms.id();

            Permission permView = aPermission()
                .withCode("PERMISSION_VIEW")
                .withName("View Permissions")
                .withTier(SecurityTier.TIER_2)
                .build();
            permissionRepository.save(permView);
            permViewPermId = permView.id();

            Permission systemPerm = aPermission()
                .withCode("PERM_MGMT_SYSTEM_PERM")
                .withName("System Permission")
                .withTier(SecurityTier.TIER_0)
                .asSystem()
                .build();
            permissionRepository.save(systemPerm);
            systemPermId = systemPerm.id();

            // Create admin role with MANAGE_PERMISSIONS permission
            Role adminRole = aRole()
                .withCode("PERM_MGMT_ADMIN")
                .withName("Permission Mgmt Admin")
                .withTier(SecurityTier.TIER_0)
                .withPermissions(Set.of(managePermsPermId, permViewPermId))
                .build();
            roleRepository.save(adminRole);
            adminRoleId = adminRole.id();

            setupComplete = true;
        }
    }

    // ==================== Create Permission Tests ====================

    @Nested
    @DisplayName("Create Permission (POST /permissions)")
    class CreatePermissionEndpoint {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should create permission with MANAGE_PERMISSIONS permission")
        void shouldCreatePermissionWithManagePermissionsPermission() {
            // Given - admin with MANAGE_PERMISSIONS
            User admin = aUser()
                .withEmail("admin-create-perm@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("PERM_MGMT_ADMIN")
                .build();

            String uniqueCode = "NEW_PERM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // When/Then - create permission should succeed
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                    {
                        "code": "%s",
                        "name": "New Test Permission",
                        "description": "A test permission",
                        "tier": "TIER_2"
                    }
                    """, uniqueCode))
            .when()
                .post(ACCESS_PATH + "/permissions")
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
        @DisplayName("should return 403 without MANAGE_PERMISSIONS permission")
        void shouldReturn403WithoutManagePermissionsPermission() {
            // Given - user without MANAGE_PERMISSIONS
            Role viewerRole = aRole()
                .withCode("PERM_VIEWER_ONLY")
                .withName("Permission Viewer Only")
                .withTier(SecurityTier.TIER_2)
                .withPermission(permViewPermId)
                .build();
            roleRepository.save(viewerRole);

            User viewer = aUser()
                .withEmail("viewer-create-perm@example.com")
                .withRoleId(viewerRole.id())
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("PERM_VIEWER_ONLY")
                .build();

            // When/Then - create permission should fail
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "code": "UNAUTHORIZED_PERM",
                        "name": "Unauthorized Permission",
                        "tier": "TIER_2"
                    }
                    """)
            .when()
                .post(ACCESS_PATH + "/permissions")
            .then()
                .statusCode(403);

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }
    }

    // ==================== Update/Delete Permission Tests ====================

    @Nested
    @DisplayName("Update/Delete Permission (PUT/DELETE /permissions/{id})")
    class UpdateDeletePermissionEndpoint {

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should update permission with MANAGE_PERMISSIONS permission")
        void shouldUpdatePermissionWithManagePermissionsPermission() {
            // Given - admin and target permission
            User admin = aUser()
                .withEmail("admin-update-perm@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            Permission targetPerm = aPermission()
                .withCode("UPDATABLE_PERM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .withName("Updatable Permission")
                .withTier(SecurityTier.TIER_2)
                .build();
            permissionRepository.save(targetPerm);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("PERM_MGMT_ADMIN")
                .build();

            // When/Then - update should succeed
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Updated Permission Name",
                        "description": "Updated description"
                    }
                    """)
            .when()
                .put(ACCESS_PATH + "/permissions/" + targetPerm.id().id())
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should return 400 for system permission modification")
        void shouldReturn400ForSystemPermissionModification() {
            // Given - admin trying to modify system permission
            User admin = aUser()
                .withEmail("admin-modify-sys-perm@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("PERM_MGMT_ADMIN")
                .build();

            // When/Then - modify system permission should fail
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Hacked System Permission"
                    }
                    """)
            .when()
                .put(ACCESS_PATH + "/permissions/" + systemPermId.id())
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should delete permission with MANAGE_PERMISSIONS permission")
        void shouldDeletePermissionWithManagePermissionsPermission() {
            // Given - admin and deletable permission
            User admin = aUser()
                .withEmail("admin-delete-perm@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            Permission deletablePerm = aPermission()
                .withCode("DELETABLE_PERM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .withName("Deletable Permission")
                .withTier(SecurityTier.TIER_2)
                .build();
            permissionRepository.save(deletablePerm);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("PERM_MGMT_ADMIN")
                .build();

            // When/Then - delete should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete(ACCESS_PATH + "/permissions/" + deletablePerm.id().id())
            .then()
                .statusCode(204);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(6)
        @Transactional
        @DisplayName("should return 400 for system permission deletion")
        void shouldReturn400ForSystemPermissionDeletion() {
            // Given - admin trying to delete system permission
            User admin = aUser()
                .withEmail("admin-delete-sys-perm@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("PERM_MGMT_ADMIN")
                .build();

            // When/Then - delete system permission should fail
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete(ACCESS_PATH + "/permissions/" + systemPermId.id())
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }
    }

    // ==================== Query Permission Tests ====================

    @Nested
    @DisplayName("Query Permissions")
    class QueryPermissionsEndpoint {

        @Test
        @Order(7)
        @Transactional
        @DisplayName("should return all permissions with PERMISSION_VIEW permission")
        void shouldReturnAllPermissionsWithPermissionViewPermission() {
            // Given - user with PERMISSION_VIEW
            User viewer = aUser()
                .withEmail("viewer-perms@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("PERM_MGMT_ADMIN")
                .build();

            // When/Then - query should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/permissions")
            .then()
                .statusCode(200)
                .body("$", not(empty()));

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }

        @Test
        @Order(8)
        @Transactional
        @DisplayName("should return permission by ID with PERMISSION_VIEW permission")
        void shouldReturnPermissionByIdWithPermissionViewPermission() {
            // Given - user with PERMISSION_VIEW
            User viewer = aUser()
                .withEmail("viewer-perm-byid@example.com")
                .withRoleId(adminRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("PERM_MGMT_ADMIN")
                .build();

            // When/Then - query should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/permissions/" + permViewPermId.id())
            .then()
                .statusCode(200)
                .body("id", equalTo(permViewPermId.id().toString()));

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }
    }
}
