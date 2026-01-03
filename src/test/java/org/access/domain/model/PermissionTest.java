package org.access.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Permission Aggregate")
class PermissionTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create standard permission with factory method")
        void shouldCreateStandardPermissionWithFactoryMethod() {
            PermissionCode code = permissionCodeOf("USER_CREATE");
            PermissionName name = permissionNameOf("Create User");
            String description = "Allows creating new users";

            Permission permission = Permission.create(code, name, description);

            assertThat(permission.id()).isNotNull();
            assertThat(permission.code()).isEqualTo(code);
            assertThat(permission.name()).isEqualTo(name);
            assertThat(permission.description()).isEqualTo(description);
            assertThat(permission.tier()).isEqualTo(SecurityTier.TIER_2);
            assertThat(permission.isSystem()).isFalse();
        }

        @Test
        @DisplayName("should create permission with explicit tier and system flag")
        void shouldCreatePermissionWithExplicitTierAndSystemFlag() {
            PermissionCode code = permissionCodeOf("SYSTEM_ADMIN");
            PermissionName name = permissionNameOf("System Admin");
            String description = "System administration permission";

            Permission permission = Permission.create(code, name, description, SecurityTier.TIER_0, true);

            assertThat(permission.tier()).isEqualTo(SecurityTier.TIER_0);
            assertThat(permission.isSystem()).isTrue();
        }

        @Test
        @DisplayName("should create permission with builder")
        void shouldCreatePermissionWithBuilder() {
            Permission permission = aPermission()
                .withCode("VIEW_USERS")
                .withName("View Users")
                .withDescription("Allows viewing users")
                .withTier(SecurityTier.TIER_1)
                .asSystem()
                .build();

            assertThat(permission.code().value()).isEqualTo("VIEW_USERS");
            assertThat(permission.name().value()).isEqualTo("View Users");
            assertThat(permission.description()).isEqualTo("Allows viewing users");
            assertThat(permission.tier()).isEqualTo(SecurityTier.TIER_1);
            assertThat(permission.isSystem()).isTrue();
        }
    }

    @Nested
    @DisplayName("Code update")
    class CodeUpdate {

        @Test
        @DisplayName("should update code")
        void shouldUpdateCode() {
            Permission permission = aPermission().withCode("OLD_CODE").build();
            PermissionCode newCode = permissionCodeOf("NEW_CODE");

            permission.updateCode(newCode);

            assertThat(permission.code()).isEqualTo(newCode);
        }

        @Test
        @DisplayName("should reject null code")
        void shouldRejectNullCode() {
            Permission permission = aPermission().build();

            assertThatThrownBy(() -> permission.updateCode(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New permission code cannot be null.");
        }
    }

    @Nested
    @DisplayName("Name update")
    class NameUpdate {

        @Test
        @DisplayName("should update name")
        void shouldUpdateName() {
            Permission permission = aPermission().withName("Old Name").build();
            PermissionName newName = permissionNameOf("New Name");

            permission.updateName(newName);

            assertThat(permission.name()).isEqualTo(newName);
        }

        @Test
        @DisplayName("should reject null name")
        void shouldRejectNullName() {
            Permission permission = aPermission().build();

            assertThatThrownBy(() -> permission.updateName(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New permission name cannot be null.");
        }
    }

    @Nested
    @DisplayName("Description update")
    class DescriptionUpdate {

        @Test
        @DisplayName("should update description")
        void shouldUpdateDescription() {
            Permission permission = aPermission().withDescription("Old description").build();
            String newDescription = "New description";

            permission.updateDescription(newDescription);

            assertThat(permission.description()).isEqualTo(newDescription);
        }

        @Test
        @DisplayName("should reject null description")
        void shouldRejectNullDescription() {
            Permission permission = aPermission().build();

            assertThatThrownBy(() -> permission.updateDescription(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New permission description cannot be null.");
        }
    }

    @Nested
    @DisplayName("System flag")
    class SystemFlag {

        @Test
        @DisplayName("asSystem builder should set isSystem to true")
        void asSystemBuilderShouldSetIsSystemToTrue() {
            Permission permission = aPermission().asSystem().build();

            assertThat(permission.isSystem()).isTrue();
        }

        @Test
        @DisplayName("asNonSystem builder should set isSystem to false")
        void asNonSystemBuilderShouldSetIsSystemToFalse() {
            Permission permission = aPermission().asNonSystem().build();

            assertThat(permission.isSystem()).isFalse();
        }
    }

    @Nested
    @DisplayName("Tier classification")
    class TierClassification {

        @Test
        @DisplayName("should support TIER_0 for critical permissions")
        void shouldSupportTier0ForCriticalPermissions() {
            Permission permission = aPermission().withTier(SecurityTier.TIER_0).build();

            assertThat(permission.tier()).isEqualTo(SecurityTier.TIER_0);
        }

        @Test
        @DisplayName("should support TIER_1 for sensitive permissions")
        void shouldSupportTier1ForSensitivePermissions() {
            Permission permission = aPermission().withTier(SecurityTier.TIER_1).build();

            assertThat(permission.tier()).isEqualTo(SecurityTier.TIER_1);
        }

        @Test
        @DisplayName("should support TIER_2 for standard permissions")
        void shouldSupportTier2ForStandardPermissions() {
            Permission permission = aPermission().withTier(SecurityTier.TIER_2).build();

            assertThat(permission.tier()).isEqualTo(SecurityTier.TIER_2);
        }

        @Test
        @DisplayName("default tier should be TIER_2")
        void defaultTierShouldBeTier2() {
            Permission permission = Permission.create(
                permissionCodeOf("TEST"),
                permissionNameOf("Test"),
                "Test description"
            );

            assertThat(permission.tier()).isEqualTo(SecurityTier.TIER_2);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when IDs are equal")
        void shouldBeEqualWhenIdsAreEqual() {
            PermissionId permissionId = randomPermissionId();
            Permission perm1 = aPermission().withId(permissionId).withCode("CODE_A").build();
            Permission perm2 = aPermission().withId(permissionId).withCode("CODE_B").build();

            assertThat(perm1).isEqualTo(perm2);
            assertThat(perm1.hashCode()).isEqualTo(perm2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            Permission perm1 = aPermission().build();
            Permission perm2 = aPermission().build();

            assertThat(perm1).isNotEqualTo(perm2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should include permission code")
        void shouldIncludePermissionCode() {
            Permission permission = aPermission().withCode("USER_DELETE").build();

            assertThat(permission.toString()).contains("Permission[USER_DELETE]");
        }
    }
}
