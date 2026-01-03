package org.access.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SecurityContext Value Object")
class SecurityContextTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create security context with all fields")
        void shouldCreateSecurityContextWithAllFields() {
            UserId actorId = randomUserId();
            String actorEmail = "actor@example.com";
            RoleId roleId = randomRoleId();
            RoleCode roleCode = roleCodeOf("ADMIN");
            SecurityTier tier = SecurityTier.TIER_1;
            Set<PermissionCode> permissions = Set.of(
                permissionCodeOf("USER_CREATE"),
                permissionCodeOf("USER_VIEW")
            );

            SecurityContext context = new SecurityContext(actorId, actorEmail, roleId, roleCode, tier, permissions);

            assertThat(context.actorId()).isEqualTo(actorId);
            assertThat(context.actorEmail()).isEqualTo(actorEmail);
            assertThat(context.roleId()).isEqualTo(roleId);
            assertThat(context.roleCode()).isEqualTo(roleCode);
            assertThat(context.actorTier()).isEqualTo(tier);
            assertThat(context.permissions()).hasSize(2);
        }

        @Test
        @DisplayName("should create security context with null permissions as empty set")
        void shouldCreateSecurityContextWithNullPermissionsAsEmptySet() {
            SecurityContext context = new SecurityContext(
                randomUserId(),
                "actor@example.com",
                randomRoleId(),
                roleCodeOf("ADMIN"),
                SecurityTier.TIER_2,
                null
            );

            assertThat(context.permissions()).isEmpty();
        }

        @Test
        @DisplayName("should make permissions immutable")
        void shouldMakePermissionsImmutable() {
            SecurityContext context = aSecurityContext()
                .withPermission("USER_CREATE")
                .build();

            assertThatThrownBy(() -> context.permissions().add(permissionCodeOf("NEW_PERM")))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should reject null actor ID")
        void shouldRejectNullActorId() {
            assertThatThrownBy(() -> new SecurityContext(
                null,
                "actor@example.com",
                randomRoleId(),
                roleCodeOf("ADMIN"),
                SecurityTier.TIER_2,
                Set.of()
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Actor ID cannot be null");
        }

        @Test
        @DisplayName("should reject null actor email")
        void shouldRejectNullActorEmail() {
            assertThatThrownBy(() -> new SecurityContext(
                randomUserId(),
                null,
                randomRoleId(),
                roleCodeOf("ADMIN"),
                SecurityTier.TIER_2,
                Set.of()
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Actor email cannot be null");
        }

        @Test
        @DisplayName("should reject null role ID")
        void shouldRejectNullRoleId() {
            assertThatThrownBy(() -> new SecurityContext(
                randomUserId(),
                "actor@example.com",
                null,
                roleCodeOf("ADMIN"),
                SecurityTier.TIER_2,
                Set.of()
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Role ID cannot be null");
        }

        @Test
        @DisplayName("should reject null role code")
        void shouldRejectNullRoleCode() {
            assertThatThrownBy(() -> new SecurityContext(
                randomUserId(),
                "actor@example.com",
                randomRoleId(),
                null,
                SecurityTier.TIER_2,
                Set.of()
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Role code cannot be null");
        }

        @Test
        @DisplayName("should reject null security tier")
        void shouldRejectNullSecurityTier() {
            assertThatThrownBy(() -> new SecurityContext(
                randomUserId(),
                "actor@example.com",
                randomRoleId(),
                roleCodeOf("ADMIN"),
                null,
                Set.of()
            ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Security tier cannot be null");
        }
    }

    @Nested
    @DisplayName("Permission checks")
    class PermissionChecks {

        @Test
        @DisplayName("hasPermission should return true when permission exists")
        void hasPermissionShouldReturnTrueWhenPermissionExists() {
            SecurityContext context = aSecurityContext()
                .withPermission("USER_CREATE")
                .withPermission("USER_VIEW")
                .build();

            assertThat(context.hasPermission(permissionCodeOf("USER_CREATE"))).isTrue();
            assertThat(context.hasPermission("USER_VIEW")).isTrue();
        }

        @Test
        @DisplayName("hasPermission should return false when permission does not exist")
        void hasPermissionShouldReturnFalseWhenPermissionDoesNotExist() {
            SecurityContext context = aSecurityContext()
                .withPermission("USER_CREATE")
                .build();

            assertThat(context.hasPermission(permissionCodeOf("USER_DELETE"))).isFalse();
            assertThat(context.hasPermission("ADMIN_ACCESS")).isFalse();
        }

        @Test
        @DisplayName("hasPermission should return false for empty permissions")
        void hasPermissionShouldReturnFalseForEmptyPermissions() {
            SecurityContext context = aSecurityContext().build();

            assertThat(context.hasPermission(permissionCodeOf("ANY_PERMISSION"))).isFalse();
        }
    }

    @Nested
    @DisplayName("Super admin checks")
    class SuperAdminChecks {

        @Test
        @DisplayName("isSuperAdmin should return true for TIER_0")
        void isSuperAdminShouldReturnTrueForTier0() {
            SecurityContext context = aSecurityContext()
                .asSuperAdmin()
                .build();

            assertThat(context.isSuperAdmin()).isTrue();
        }

        @Test
        @DisplayName("isSuperAdmin should return false for TIER_1")
        void isSuperAdminShouldReturnFalseForTier1() {
            SecurityContext context = aSecurityContext()
                .asDirector()
                .build();

            assertThat(context.isSuperAdmin()).isFalse();
        }

        @Test
        @DisplayName("isSuperAdmin should return false for TIER_2")
        void isSuperAdminShouldReturnFalseForTier2() {
            SecurityContext context = aSecurityContext()
                .asHrManager()
                .build();

            assertThat(context.isSuperAdmin()).isFalse();
        }
    }

    @Nested
    @DisplayName("Tier management checks")
    class TierManagementChecks {

        @Test
        @DisplayName("TIER_0 can manage TIER_1 and TIER_2")
        void tier0CanManageLowerTiers() {
            SecurityContext context = aSecurityContext()
                .asSuperAdmin()
                .build();

            assertThat(context.canManageTier(SecurityTier.TIER_0)).isFalse();
            assertThat(context.canManageTier(SecurityTier.TIER_1)).isTrue();
            assertThat(context.canManageTier(SecurityTier.TIER_2)).isTrue();
        }

        @Test
        @DisplayName("TIER_1 can only manage TIER_2")
        void tier1CanOnlyManageTier2() {
            SecurityContext context = aSecurityContext()
                .asDirector()
                .build();

            assertThat(context.canManageTier(SecurityTier.TIER_0)).isFalse();
            assertThat(context.canManageTier(SecurityTier.TIER_1)).isFalse();
            assertThat(context.canManageTier(SecurityTier.TIER_2)).isTrue();
        }

        @Test
        @DisplayName("TIER_2 cannot manage any tier")
        void tier2CannotManageAnyTier() {
            SecurityContext context = aSecurityContext()
                .asHrManager()
                .build();

            assertThat(context.canManageTier(SecurityTier.TIER_0)).isFalse();
            assertThat(context.canManageTier(SecurityTier.TIER_1)).isFalse();
            assertThat(context.canManageTier(SecurityTier.TIER_2)).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder shortcuts")
    class BuilderShortcuts {

        @Test
        @DisplayName("asSuperAdmin should set TIER_0 and SUPER_ADMIN role code")
        void asSuperAdminShouldSetCorrectValues() {
            SecurityContext context = aSecurityContext()
                .asSuperAdmin()
                .build();

            assertThat(context.actorTier()).isEqualTo(SecurityTier.TIER_0);
            assertThat(context.roleCode().value()).isEqualTo("SUPER_ADMIN");
        }

        @Test
        @DisplayName("asDirector should set TIER_1 and DIRECTOR role code")
        void asDirectorShouldSetCorrectValues() {
            SecurityContext context = aSecurityContext()
                .asDirector()
                .build();

            assertThat(context.actorTier()).isEqualTo(SecurityTier.TIER_1);
            assertThat(context.roleCode().value()).isEqualTo("DIRECTOR");
        }

        @Test
        @DisplayName("asHrManager should set TIER_2 and HR_MANAGER role code")
        void asHrManagerShouldSetCorrectValues() {
            SecurityContext context = aSecurityContext()
                .asHrManager()
                .build();

            assertThat(context.actorTier()).isEqualTo(SecurityTier.TIER_2);
            assertThat(context.roleCode().value()).isEqualTo("HR_MANAGER");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should include relevant context information")
        void shouldIncludeRelevantContextInformation() {
            SecurityContext context = aSecurityContext()
                .withActorEmail("admin@example.com")
                .asSuperAdmin()
                .build();

            String str = context.toString();

            assertThat(str).contains("admin@example.com");
            assertThat(str).contains("SUPER_ADMIN");
            assertThat(str).contains("TIER_0");
        }
    }
}
