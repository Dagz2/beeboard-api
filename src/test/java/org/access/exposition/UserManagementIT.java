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
 * Integration tests for User Management endpoints in AccessResource.
 *
 * Tests user creation, locking/unlocking, role assignment, updates, and deletion.
 */
@QuarkusTest
@DisplayName("User Management Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagementIT {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    PermissionRepository permissionRepository;

    private static RoleId tier0RoleId;
    private static RoleId tier1RoleId;
    private static RoleId tier2RoleId;
    private static PermissionId userCreatePermId;
    private static PermissionId userLockPermId;
    private static PermissionId userUpdatePermId;
    private static PermissionId userDeletePermId;
    private static PermissionId userViewAllPermId;
    private static PermissionId assignBasicRolesPermId;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            // Create permissions
            Permission userCreate = aPermission().withCode("USER_CREATE").withName("Create User").withTier(SecurityTier.TIER_1).build();
            Permission userLock = aPermission().withCode("USER_LOCK").withName("Lock User").withTier(SecurityTier.TIER_1).build();
            Permission userUpdate = aPermission().withCode("USER_UPDATE").withName("Update User").withTier(SecurityTier.TIER_1).build();
            Permission userDelete = aPermission().withCode("USER_DELETE").withName("Delete User").withTier(SecurityTier.TIER_1).build();
            Permission userViewAll = aPermission().withCode("USER_VIEW_ALL").withName("View All Users").withTier(SecurityTier.TIER_2).build();
            Permission assignBasic = aPermission().withCode("ASSIGN_BASIC_ROLES").withName("Assign Basic Roles").withTier(SecurityTier.TIER_1).build();

            permissionRepository.save(userCreate);
            permissionRepository.save(userLock);
            permissionRepository.save(userUpdate);
            permissionRepository.save(userDelete);
            permissionRepository.save(userViewAll);
            permissionRepository.save(assignBasic);

            userCreatePermId = userCreate.id();
            userLockPermId = userLock.id();
            userUpdatePermId = userUpdate.id();
            userDeletePermId = userDelete.id();
            userViewAllPermId = userViewAll.id();
            assignBasicRolesPermId = assignBasic.id();

            // Create TIER_0 role (Super Admin)
            Role tier0Role = aRole()
                .withCode("USER_MGMT_SUPER_ADMIN")
                .withName("User Mgmt Super Admin")
                .withTier(SecurityTier.TIER_0)
                .withPermissions(Set.of(userCreatePermId, userLockPermId, userUpdatePermId, userDeletePermId, userViewAllPermId, assignBasicRolesPermId))
                .asSystem()
                .build();
            roleRepository.save(tier0Role);
            tier0RoleId = tier0Role.id();

            // Create TIER_1 role (Director)
            Role tier1Role = aRole()
                .withCode("USER_MGMT_DIRECTOR")
                .withName("User Mgmt Director")
                .withTier(SecurityTier.TIER_1)
                .withPermissions(Set.of(userCreatePermId, userLockPermId, userUpdatePermId, userViewAllPermId, assignBasicRolesPermId))
                .build();
            roleRepository.save(tier1Role);
            tier1RoleId = tier1Role.id();

            // Create TIER_2 role (HR Manager)
            Role tier2Role = aRole()
                .withCode("USER_MGMT_HR_MANAGER")
                .withName("User Mgmt HR Manager")
                .withTier(SecurityTier.TIER_2)
                .withPermission(userViewAllPermId)
                .build();
            roleRepository.save(tier2Role);
            tier2RoleId = tier2Role.id();

            setupComplete = true;
        }
    }

    // ==================== Create User Tests ====================

    @Nested
    @DisplayName("Create User (POST /users)")
    class CreateUserEndpoint {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should create user with USER_CREATE permission")
        void shouldCreateUserWithUserCreatePermission() {
            // Given - a user with USER_CREATE permission
            User admin = aUser()
                .withEmail("admin-create@example.com")
                .withRoleId(tier1RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_DIRECTOR")
                .build();

            // When/Then - create user should succeed
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                    {
                        "email": "new-user-%s@example.com",
                        "firstName": "New",
                        "lastName": "User",
                        "password": "SecurePassword123!@#$%%^&*()_+abcd",
                        "roleId": "%s",
                        "contractEndDate": "%s"
                    }
                    """, UUID.randomUUID().toString().substring(0, 8), tier2RoleId.id(), LocalDate.now().plusYears(1)))
            .when()
                .post(ACCESS_PATH + "/users")
            .then()
                .statusCode(201)
                .body("id", notNullValue());

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should return 403 without USER_CREATE permission")
        void shouldReturn403WithoutUserCreatePermission() {
            // Given - a user without USER_CREATE permission
            User viewer = aUser()
                .withEmail("viewer-nocreate@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("USER_MGMT_HR_MANAGER")
                .build();

            // When/Then - create user should fail
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                    {
                        "email": "new-user@example.com",
                        "firstName": "New",
                        "lastName": "User",
                        "password": "SecurePassword123!@#$%%^&*()_+abcd",
                        "roleId": "%s",
                        "contractEndDate": "%s"
                    }
                    """, tier2RoleId.id(), LocalDate.now().plusYears(1)))
            .when()
                .post(ACCESS_PATH + "/users")
            .then()
                .statusCode(403);

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }
    }

    // ==================== Lock/Unlock User Tests ====================

    @Nested
    @DisplayName("Lock/Unlock User (PUT /users/{id}/lock, /unlock)")
    class LockUnlockUserEndpoint {

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should lock user with USER_LOCK permission")
        void shouldLockUserWithUserLockPermission() {
            // Given - admin and target user
            User admin = aUser()
                .withEmail("admin-lock@example.com")
                .withRoleId(tier1RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            User target = aUser()
                .withEmail("target-lock@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(target);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_DIRECTOR")
                .build();

            // When/Then - lock should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .put(ACCESS_PATH + "/users/" + target.getId().id() + "/lock")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
            userRepository.deleteById(target.getId());
        }

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should unlock user with USER_LOCK permission")
        void shouldUnlockUserWithUserLockPermission() {
            // Given - admin and locked target user
            User admin = aUser()
                .withEmail("admin-unlock@example.com")
                .withRoleId(tier1RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            User target = aUser()
                .withEmail("target-unlock@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .locked()
                .build();
            userRepository.save(target);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_DIRECTOR")
                .build();

            // When/Then - unlock should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .put(ACCESS_PATH + "/users/" + target.getId().id() + "/unlock")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
            userRepository.deleteById(target.getId());
        }

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should prevent self-lock")
        void shouldPreventSelfLock() {
            // Given - a user trying to lock themselves
            User admin = aUser()
                .withEmail("admin-selflock@example.com")
                .withRoleId(tier1RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_DIRECTOR")
                .build();

            // When/Then - self-lock should fail
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .put(ACCESS_PATH + "/users/" + admin.getId().id() + "/lock")
            .then()
                .statusCode(anyOf(is(400), is(403)));

            // Cleanup
            userRepository.deleteById(admin.getId());
        }
    }

    // ==================== Role Assignment Tests ====================

    @Nested
    @DisplayName("Role Assignment (PUT /users/{id}/role)")
    class RoleAssignmentEndpoint {

        @Test
        @Order(6)
        @Transactional
        @DisplayName("should assign role with correct permission")
        void shouldAssignRoleWithCorrectPermission() {
            // Given - admin and target user
            User admin = aUser()
                .withEmail("admin-assign@example.com")
                .withRoleId(tier1RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            User target = aUser()
                .withEmail("target-assign@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(target);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_DIRECTOR")
                .build();

            // When/Then - role assignment should succeed
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                    {
                        "roleId": "%s"
                    }
                    """, tier2RoleId.id()))
            .when()
                .put(ACCESS_PATH + "/users/" + target.getId().id() + "/role")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
            userRepository.deleteById(target.getId());
        }

        @Test
        @Order(7)
        @Transactional
        @DisplayName("should prevent self-role assignment")
        void shouldPreventSelfRoleAssignment() {
            // Given - a user trying to change their own role
            User admin = aUser()
                .withEmail("admin-selfrole@example.com")
                .withRoleId(tier1RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_DIRECTOR")
                .build();

            // When/Then - self-assignment should fail
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                    {
                        "roleId": "%s"
                    }
                    """, tier0RoleId.id()))
            .when()
                .put(ACCESS_PATH + "/users/" + admin.getId().id() + "/role")
            .then()
                .statusCode(anyOf(is(400), is(403)));

            // Cleanup
            userRepository.deleteById(admin.getId());
        }
    }

    // ==================== Delete User Tests ====================

    @Nested
    @DisplayName("Delete User (DELETE /users/{id})")
    class DeleteUserEndpoint {

        @Test
        @Order(8)
        @Transactional
        @DisplayName("should delete locked user with USER_DELETE permission")
        void shouldDeleteLockedUser() {
            // Given - admin and locked target user
            User admin = aUser()
                .withEmail("admin-delete@example.com")
                .withRoleId(tier0RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            User target = aUser()
                .withEmail("target-delete@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .locked() // Must be locked first
                .build();
            userRepository.save(target);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_SUPER_ADMIN")
                .build();

            // When/Then - delete should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete(ACCESS_PATH + "/users/" + target.getId().id())
            .then()
                .statusCode(204);

            // Cleanup
            userRepository.deleteById(admin.getId());
        }

        @Test
        @Order(9)
        @Transactional
        @DisplayName("should return 400 for non-locked user deletion")
        void shouldReturn400ForNonLockedUser() {
            // Given - admin and unlocked target user
            User admin = aUser()
                .withEmail("admin-delete-fail@example.com")
                .withRoleId(tier0RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            User target = aUser()
                .withEmail("target-delete-fail@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked() // Not locked
                .build();
            userRepository.save(target);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_SUPER_ADMIN")
                .build();

            // When/Then - delete should fail
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete(ACCESS_PATH + "/users/" + target.getId().id())
            .then()
                .statusCode(400);

            // Cleanup
            userRepository.deleteById(admin.getId());
            userRepository.deleteById(target.getId());
        }

        @Test
        @Order(10)
        @Transactional
        @DisplayName("should prevent self-deletion")
        void shouldPreventSelfDeletion() {
            // Given - a user trying to delete themselves
            User admin = aUser()
                .withEmail("admin-selfdelete@example.com")
                .withRoleId(tier0RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .locked() // Even if locked
                .build();
            userRepository.save(admin);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_SUPER_ADMIN")
                .build();

            // When/Then - self-deletion should fail
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete(ACCESS_PATH + "/users/" + admin.getId().id())
            .then()
                .statusCode(anyOf(is(400), is(403)));

            // Cleanup
            userRepository.deleteById(admin.getId());
        }
    }

    // ==================== Update User Tests ====================

    @Nested
    @DisplayName("Update User (PUT /users/{id}/name, /email, /password, /contract)")
    class UpdateUserEndpoint {

        @Test
        @Order(11)
        @Transactional
        @DisplayName("should update name with USER_UPDATE permission")
        void shouldUpdateNameWithUserUpdatePermission() {
            // Given - admin and target user
            User admin = aUser()
                .withEmail("admin-update-name@example.com")
                .withRoleId(tier1RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            User target = aUser()
                .withEmail("target-update-name@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(target);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_DIRECTOR")
                .build();

            // When/Then - update name should succeed
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                    {
                        "firstName": "Updated",
                        "lastName": "Name"
                    }
                    """)
            .when()
                .put(ACCESS_PATH + "/users/" + target.getId().id() + "/name")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
            userRepository.deleteById(target.getId());
        }

        @Test
        @Order(12)
        @Transactional
        @DisplayName("should extend contract with USER_UPDATE permission")
        void shouldExtendContractWithUserUpdatePermission() {
            // Given - admin and target user
            User admin = aUser()
                .withEmail("admin-extend@example.com")
                .withRoleId(tier1RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(admin);

            User target = aUser()
                .withEmail("target-extend@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusMonths(1))
                .unlocked()
                .build();
            userRepository.save(target);

            String token = aToken()
                .forUser(admin.getId())
                .withEmail(admin.getEmail().value())
                .withRole("USER_MGMT_DIRECTOR")
                .build();

            // When/Then - extend contract should succeed
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                    {
                        "contractEndDate": "%s"
                    }
                    """, LocalDate.now().plusYears(2)))
            .when()
                .put(ACCESS_PATH + "/users/" + target.getId().id() + "/contract")
            .then()
                .statusCode(200);

            // Cleanup
            userRepository.deleteById(admin.getId());
            userRepository.deleteById(target.getId());
        }
    }

    // ==================== Query Tests ====================

    @Nested
    @DisplayName("Query Users")
    class QueryUsersEndpoint {

        @Test
        @Order(13)
        @Transactional
        @DisplayName("should return all users with USER_VIEW_ALL permission")
        void shouldReturnAllUsersWithUserViewAllPermission() {
            // Given - a user with USER_VIEW_ALL permission
            User viewer = aUser()
                .withEmail("viewer-all@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("USER_MGMT_HR_MANAGER")
                .build();

            // When/Then - query should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/users")
            .then()
                .statusCode(200)
                .body("$", not(empty()));

            // Cleanup
            userRepository.deleteById(viewer.getId());
        }

        @Test
        @Order(14)
        @Transactional
        @DisplayName("should return user by ID with USER_VIEW_ALL permission")
        void shouldReturnUserByIdWithUserViewAllPermission() {
            // Given - a viewer and target user
            User viewer = aUser()
                .withEmail("viewer-byid@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(viewer);

            User target = aUser()
                .withEmail("target-byid@example.com")
                .withRoleId(tier2RoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(target);

            String token = aToken()
                .forUser(viewer.getId())
                .withEmail(viewer.getEmail().value())
                .withRole("USER_MGMT_HR_MANAGER")
                .build();

            // When/Then - query should succeed
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get(ACCESS_PATH + "/users/" + target.getId().id())
            .then()
                .statusCode(200)
                .body("id", equalTo(target.getId().id().toString()));

            // Cleanup
            userRepository.deleteById(viewer.getId());
            userRepository.deleteById(target.getId());
        }
    }
}
