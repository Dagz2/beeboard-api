package org.access.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.shared.domain.ApplicationRole;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Role Entity")
class RoleTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create standard role with factory method")
        void shouldCreateStandardRoleWithFactoryMethod() {
            RoleCode code = roleCodeOf("CUSTOM_ROLE");
            RoleName name = roleNameOf("Custom Role");

            Role role = Role.create(code, name);

            assertThat(role.id()).isNotNull();
            assertThat(role.code()).isEqualTo(code);
            assertThat(role.name()).isEqualTo(name);
            assertThat(role.tier()).isEqualTo(SecurityTier.TIER_2);
            assertThat(role.isSystem()).isFalse();
            assertThat(role.permissions()).isEmpty();
            assertThat(role.auditInfo()).isNotNull();
        }

        @Test
        @DisplayName("should create role with explicit tier and system flag")
        void shouldCreateRoleWithExplicitTierAndSystemFlag() {
            RoleCode code = roleCodeOf("ADMIN_ROLE");
            RoleName name = roleNameOf("Admin Role");

            Role role = Role.create(code, name, SecurityTier.TIER_1, true, ApplicationRole.SUPERVISOR);

            assertThat(role.tier()).isEqualTo(SecurityTier.TIER_1);
            assertThat(role.isSystem()).isTrue();
        }

        @Test
        @DisplayName("should create role with builder")
        void shouldCreateRoleWithBuilder() {
            Role role = aRole()
                .withCode("TEST_ROLE")
                .withName("Test Role")
                .withTier(SecurityTier.TIER_1)
                .asSystem()
                .build();

            assertThat(role.code().value()).isEqualTo("TEST_ROLE");
            assertThat(role.name().value()).isEqualTo("Test Role");
            assertThat(role.tier()).isEqualTo(SecurityTier.TIER_1);
            assertThat(role.isSystem()).isTrue();
        }
    }

    @Nested
    @DisplayName("Permission management")
    class PermissionManagement {

        @Test
        @DisplayName("should grant permission")
        void shouldGrantPermission() {
            Role role = aRole().build();
            PermissionId permissionId = randomPermissionId();

            boolean added = role.grantPermission(permissionId);

            assertThat(added).isTrue();
            assertThat(role.permissions()).contains(permissionId);
            assertThat(role.hasPermission(permissionId)).isTrue();
        }

        @Test
        @DisplayName("should return false when granting duplicate permission")
        void shouldReturnFalseWhenGrantingDuplicatePermission() {
            Role role = aRole().build();
            PermissionId permissionId = randomPermissionId();

            role.grantPermission(permissionId);
            boolean addedAgain = role.grantPermission(permissionId);

            assertThat(addedAgain).isFalse();
            assertThat(role.permissions()).hasSize(1);
        }

        @Test
        @DisplayName("should reject null permission ID for grant")
        void shouldRejectNullPermissionIdForGrant() {
            Role role = aRole().build();

            assertThatThrownBy(() -> role.grantPermission(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Permission ID cannot be null.");
        }

        @Test
        @DisplayName("should revoke permission")
        void shouldRevokePermission() {
            PermissionId permissionId = randomPermissionId();
            Role role = aRole().withPermission(permissionId).build();

            boolean removed = role.revokePermission(permissionId);

            assertThat(removed).isTrue();
            assertThat(role.permissions()).doesNotContain(permissionId);
            assertThat(role.hasPermission(permissionId)).isFalse();
        }

        @Test
        @DisplayName("should return false when revoking non-existent permission")
        void shouldReturnFalseWhenRevokingNonExistentPermission() {
            Role role = aRole().build();
            PermissionId permissionId = randomPermissionId();

            boolean removed = role.revokePermission(permissionId);

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("should reject null permission ID for revoke")
        void shouldRejectNullPermissionIdForRevoke() {
            Role role = aRole().build();

            assertThatThrownBy(() -> role.revokePermission(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Permission ID cannot be null.");
        }

        @Test
        @DisplayName("permissions should be unmodifiable from outside")
        void permissionsShouldBeUnmodifiableFromOutside() {
            Role role = aRole().build();

            assertThatThrownBy(() -> role.permissions().add(randomPermissionId()))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Code and name updates")
    class CodeAndNameUpdates {

        @Test
        @DisplayName("should update code")
        void shouldUpdateCode() {
            Role role = aRole().withCode("OLD_CODE").build();
            RoleCode newCode = roleCodeOf("NEW_CODE");

            role.updateCode(newCode);

            assertThat(role.code()).isEqualTo(newCode);
        }

        @Test
        @DisplayName("should reject null code")
        void shouldRejectNullCode() {
            Role role = aRole().build();

            assertThatThrownBy(() -> role.updateCode(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New role code cannot be null.");
        }

        @Test
        @DisplayName("should update name")
        void shouldUpdateName() {
            Role role = aRole().withName("Old Name").build();
            RoleName newName = roleNameOf("New Name");

            role.updateName(newName);

            assertThat(role.name()).isEqualTo(newName);
        }

        @Test
        @DisplayName("should reject null name")
        void shouldRejectNullName() {
            Role role = aRole().build();

            assertThatThrownBy(() -> role.updateName(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New role name cannot be null.");
        }
    }

    @Nested
    @DisplayName("System role builders")
    class SystemRoleBuilders {

        @Test
        @DisplayName("asSuperAdmin should create TIER_0 system role")
        void asSuperAdminShouldCreateTier0SystemRole() {
            Role role = aRole().asSuperAdmin().build();

            assertThat(role.code().value()).isEqualTo("SUPER_ADMIN");
            assertThat(role.name().value()).isEqualTo("Super Administrator");
            assertThat(role.tier()).isEqualTo(SecurityTier.TIER_0);
            assertThat(role.isSystem()).isTrue();
        }

        @Test
        @DisplayName("asDirector should create TIER_1 system role")
        void asDirectorShouldCreateTier1SystemRole() {
            Role role = aRole().asDirector().build();

            assertThat(role.code().value()).isEqualTo("DIRECTOR");
            assertThat(role.name().value()).isEqualTo("Director");
            assertThat(role.tier()).isEqualTo(SecurityTier.TIER_1);
            assertThat(role.isSystem()).isTrue();
        }

        @Test
        @DisplayName("asHrManager should create TIER_2 system role")
        void asHrManagerShouldCreateTier2SystemRole() {
            Role role = aRole().asHrManager().build();

            assertThat(role.code().value()).isEqualTo("HR_MANAGER");
            assertThat(role.name().value()).isEqualTo("HR Manager");
            assertThat(role.tier()).isEqualTo(SecurityTier.TIER_2);
            assertThat(role.isSystem()).isTrue();
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when IDs are equal")
        void shouldBeEqualWhenIdsAreEqual() {
            RoleId roleId = randomRoleId();
            Role role1 = aRole().withId(roleId).withCode("CODE_A").build();
            Role role2 = aRole().withId(roleId).withCode("CODE_B").build();

            assertThat(role1).isEqualTo(role2);
            assertThat(role1.hashCode()).isEqualTo(role2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            Role role1 = aRole().build();
            Role role2 = aRole().build();

            assertThat(role1).isNotEqualTo(role2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should include role code")
        void shouldIncludeRoleCode() {
            Role role = aRole().withCode("ADMIN").build();

            assertThat(role.toString()).contains("Role[ADMIN]");
        }
    }
}
