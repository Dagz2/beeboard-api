package org.access.domain.service;

import org.access.domain.exception.InsufficientTierException;
import org.access.domain.exception.PrivilegeEscalationException;
import org.access.domain.exception.SelfModificationForbiddenException;
import org.access.domain.exception.SystemResourceProtectedException;
import org.access.domain.model.Permission;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityTier;
import org.access.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PrivilegeEscalationGuard Domain Service")
class PrivilegeEscalationGuardTest {

    private PrivilegeEscalationGuard guard;

    @BeforeEach
    void setUp() {
        guard = new PrivilegeEscalationGuard();
    }

    @Nested
    @DisplayName("Self-modification protection")
    class SelfModificationProtection {

        @Test
        @DisplayName("should throw when actor tries to modify their own role")
        void shouldThrowWhenActorTriesToModifyOwnRole() {
            UserId userId = randomUserId();

            assertThatThrownBy(() -> guard.validateNotSelfModification(userId, userId))
                .isInstanceOf(SelfModificationForbiddenException.class);
        }

        @Test
        @DisplayName("should allow modifying other users")
        void shouldAllowModifyingOtherUsers() {
            UserId actorId = randomUserId();
            UserId targetId = randomUserId();

            assertThatCode(() -> guard.validateNotSelfModification(actorId, targetId))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Role assignment validation")
    class RoleAssignmentValidation {

        @ParameterizedTest
        @CsvSource({
            "TIER_0, TIER_1, true",
            "TIER_0, TIER_2, true",
            "TIER_1, TIER_2, true",
            "TIER_0, TIER_0, false",
            "TIER_1, TIER_0, false",
            "TIER_1, TIER_1, false",
            "TIER_2, TIER_0, false",
            "TIER_2, TIER_1, false",
            "TIER_2, TIER_2, false"
        })
        @DisplayName("should validate role assignment based on tier hierarchy")
        void shouldValidateRoleAssignmentBasedOnTierHierarchy(
                SecurityTier actorTier,
                SecurityTier roleTier,
                boolean shouldSucceed) {

            Role role = aRole().withTier(roleTier).build();

            if (shouldSucceed) {
                assertThatCode(() -> guard.validateRoleAssignment(actorTier, role))
                    .doesNotThrowAnyException();
            } else {
                assertThatThrownBy(() -> guard.validateRoleAssignment(actorTier, role))
                    .isInstanceOf(PrivilegeEscalationException.class);
            }
        }

        @Test
        @DisplayName("TIER_0 can assign TIER_1 and TIER_2 roles")
        void tier0CanAssignLowerTierRoles() {
            Role tier1Role = aRole().withTier(SecurityTier.TIER_1).build();
            Role tier2Role = aRole().withTier(SecurityTier.TIER_2).build();

            assertThatCode(() -> guard.validateRoleAssignment(SecurityTier.TIER_0, tier1Role))
                .doesNotThrowAnyException();
            assertThatCode(() -> guard.validateRoleAssignment(SecurityTier.TIER_0, tier2Role))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("TIER_0 cannot assign TIER_0 role")
        void tier0CannotAssignTier0Role() {
            Role tier0Role = aRole().withTier(SecurityTier.TIER_0).build();

            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_0, tier0Role))
                .isInstanceOf(PrivilegeEscalationException.class);
        }

        @Test
        @DisplayName("TIER_1 can only assign TIER_2 roles")
        void tier1CanOnlyAssignTier2Roles() {
            Role tier0Role = aRole().withTier(SecurityTier.TIER_0).build();
            Role tier1Role = aRole().withTier(SecurityTier.TIER_1).build();
            Role tier2Role = aRole().withTier(SecurityTier.TIER_2).build();

            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_1, tier0Role))
                .isInstanceOf(PrivilegeEscalationException.class);
            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_1, tier1Role))
                .isInstanceOf(PrivilegeEscalationException.class);
            assertThatCode(() -> guard.validateRoleAssignment(SecurityTier.TIER_1, tier2Role))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("TIER_2 cannot assign any role")
        void tier2CannotAssignAnyRole() {
            Role tier0Role = aRole().withTier(SecurityTier.TIER_0).build();
            Role tier1Role = aRole().withTier(SecurityTier.TIER_1).build();
            Role tier2Role = aRole().withTier(SecurityTier.TIER_2).build();

            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_2, tier0Role))
                .isInstanceOf(PrivilegeEscalationException.class);
            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_2, tier1Role))
                .isInstanceOf(PrivilegeEscalationException.class);
            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_2, tier2Role))
                .isInstanceOf(PrivilegeEscalationException.class);
        }
    }

    @Nested
    @DisplayName("Permission grant validation")
    class PermissionGrantValidation {

        @ParameterizedTest
        @CsvSource({
            "TIER_0, TIER_1, true",
            "TIER_0, TIER_2, true",
            "TIER_1, TIER_2, true",
            "TIER_0, TIER_0, false",
            "TIER_1, TIER_0, false",
            "TIER_1, TIER_1, false",
            "TIER_2, TIER_0, false",
            "TIER_2, TIER_1, false",
            "TIER_2, TIER_2, false"
        })
        @DisplayName("should validate permission grant based on tier hierarchy")
        void shouldValidatePermissionGrantBasedOnTierHierarchy(
                SecurityTier actorTier,
                SecurityTier permissionTier,
                boolean shouldSucceed) {

            Permission permission = aPermission().withTier(permissionTier).build();

            if (shouldSucceed) {
                assertThatCode(() -> guard.validatePermissionGrant(actorTier, permission))
                    .doesNotThrowAnyException();
            } else {
                assertThatThrownBy(() -> guard.validatePermissionGrant(actorTier, permission))
                    .isInstanceOf(PrivilegeEscalationException.class);
            }
        }

        @Test
        @DisplayName("TIER_0 cannot grant TIER_0 permissions")
        void tier0CannotGrantTier0Permissions() {
            Permission tier0Permission = aPermission().withTier(SecurityTier.TIER_0).build();

            assertThatThrownBy(() -> guard.validatePermissionGrant(SecurityTier.TIER_0, tier0Permission))
                .isInstanceOf(PrivilegeEscalationException.class);
        }
    }

    @Nested
    @DisplayName("System role modification protection")
    class SystemRoleModificationProtection {

        @Test
        @DisplayName("TIER_0 can modify system roles")
        void tier0CanModifySystemRoles() {
            Role systemRole = aRole().asSystem().build();

            assertThatCode(() -> guard.validateSystemRoleModification(SecurityTier.TIER_0, systemRole))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("TIER_1 cannot modify system roles")
        void tier1CannotModifySystemRoles() {
            Role systemRole = aRole().asSystem().build();

            assertThatThrownBy(() -> guard.validateSystemRoleModification(SecurityTier.TIER_1, systemRole))
                .isInstanceOf(SystemResourceProtectedException.class);
        }

        @Test
        @DisplayName("TIER_2 cannot modify system roles")
        void tier2CannotModifySystemRoles() {
            Role systemRole = aRole().asSystem().build();

            assertThatThrownBy(() -> guard.validateSystemRoleModification(SecurityTier.TIER_2, systemRole))
                .isInstanceOf(SystemResourceProtectedException.class);
        }

        @Test
        @DisplayName("any tier can modify non-system roles")
        void anyTierCanModifyNonSystemRoles() {
            Role nonSystemRole = aRole().asNonSystem().build();

            assertThatCode(() -> guard.validateSystemRoleModification(SecurityTier.TIER_0, nonSystemRole))
                .doesNotThrowAnyException();
            assertThatCode(() -> guard.validateSystemRoleModification(SecurityTier.TIER_1, nonSystemRole))
                .doesNotThrowAnyException();
            assertThatCode(() -> guard.validateSystemRoleModification(SecurityTier.TIER_2, nonSystemRole))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Role deletion protection")
    class RoleDeletionProtection {

        @Test
        @DisplayName("should prevent deletion of system roles")
        void shouldPreventDeletionOfSystemRoles() {
            Role systemRole = aRole().asSystem().build();

            assertThatThrownBy(() -> guard.validateRoleDeletion(systemRole))
                .isInstanceOf(SystemResourceProtectedException.class);
        }

        @Test
        @DisplayName("should allow deletion of non-system roles")
        void shouldAllowDeletionOfNonSystemRoles() {
            Role nonSystemRole = aRole().asNonSystem().build();

            assertThatCode(() -> guard.validateRoleDeletion(nonSystemRole))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("SUPER_ADMIN role cannot be deleted")
        void superAdminRoleCannotBeDeleted() {
            Role superAdminRole = aRole().asSuperAdmin().build();

            assertThatThrownBy(() -> guard.validateRoleDeletion(superAdminRole))
                .isInstanceOf(SystemResourceProtectedException.class);
        }

        @Test
        @DisplayName("DIRECTOR role cannot be deleted")
        void directorRoleCannotBeDeleted() {
            Role directorRole = aRole().asDirector().build();

            assertThatThrownBy(() -> guard.validateRoleDeletion(directorRole))
                .isInstanceOf(SystemResourceProtectedException.class);
        }

        @Test
        @DisplayName("HR_MANAGER role cannot be deleted")
        void hrManagerRoleCannotBeDeleted() {
            Role hrManagerRole = aRole().asHrManager().build();

            assertThatThrownBy(() -> guard.validateRoleDeletion(hrManagerRole))
                .isInstanceOf(SystemResourceProtectedException.class);
        }
    }

    @Nested
    @DisplayName("System permission modification protection")
    class SystemPermissionModificationProtection {

        @Test
        @DisplayName("TIER_0 can modify system permissions")
        void tier0CanModifySystemPermissions() {
            Permission systemPermission = aPermission().asSystem().build();

            assertThatCode(() -> guard.validateSystemPermissionModification(SecurityTier.TIER_0, systemPermission))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("TIER_1 cannot modify system permissions")
        void tier1CannotModifySystemPermissions() {
            Permission systemPermission = aPermission().asSystem().build();

            assertThatThrownBy(() -> guard.validateSystemPermissionModification(SecurityTier.TIER_1, systemPermission))
                .isInstanceOf(SystemResourceProtectedException.class);
        }

        @Test
        @DisplayName("TIER_2 cannot modify system permissions")
        void tier2CannotModifySystemPermissions() {
            Permission systemPermission = aPermission().asSystem().build();

            assertThatThrownBy(() -> guard.validateSystemPermissionModification(SecurityTier.TIER_2, systemPermission))
                .isInstanceOf(SystemResourceProtectedException.class);
        }

        @Test
        @DisplayName("any tier can modify non-system permissions")
        void anyTierCanModifyNonSystemPermissions() {
            Permission nonSystemPermission = aPermission().asNonSystem().build();

            assertThatCode(() -> guard.validateSystemPermissionModification(SecurityTier.TIER_0, nonSystemPermission))
                .doesNotThrowAnyException();
            assertThatCode(() -> guard.validateSystemPermissionModification(SecurityTier.TIER_1, nonSystemPermission))
                .doesNotThrowAnyException();
            assertThatCode(() -> guard.validateSystemPermissionModification(SecurityTier.TIER_2, nonSystemPermission))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Permission deletion protection")
    class PermissionDeletionProtection {

        @Test
        @DisplayName("should prevent deletion of system permissions")
        void shouldPreventDeletionOfSystemPermissions() {
            Permission systemPermission = aPermission().asSystem().build();

            assertThatThrownBy(() -> guard.validatePermissionDeletion(systemPermission))
                .isInstanceOf(SystemResourceProtectedException.class);
        }

        @Test
        @DisplayName("should allow deletion of non-system permissions")
        void shouldAllowDeletionOfNonSystemPermissions() {
            Permission nonSystemPermission = aPermission().asNonSystem().build();

            assertThatCode(() -> guard.validatePermissionDeletion(nonSystemPermission))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Minimum tier validation")
    class MinimumTierValidation {

        @Test
        @DisplayName("should pass when actor tier meets minimum requirement")
        void shouldPassWhenActorTierMeetsMinimumRequirement() {
            assertThatCode(() -> guard.validateMinimumTier(SecurityTier.TIER_0, SecurityTier.TIER_0))
                .doesNotThrowAnyException();
            assertThatCode(() -> guard.validateMinimumTier(SecurityTier.TIER_0, SecurityTier.TIER_1))
                .doesNotThrowAnyException();
            assertThatCode(() -> guard.validateMinimumTier(SecurityTier.TIER_1, SecurityTier.TIER_1))
                .doesNotThrowAnyException();
            assertThatCode(() -> guard.validateMinimumTier(SecurityTier.TIER_2, SecurityTier.TIER_2))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw when actor tier is insufficient")
        void shouldThrowWhenActorTierIsInsufficient() {
            assertThatThrownBy(() -> guard.validateMinimumTier(SecurityTier.TIER_1, SecurityTier.TIER_0))
                .isInstanceOf(InsufficientTierException.class);
            assertThatThrownBy(() -> guard.validateMinimumTier(SecurityTier.TIER_2, SecurityTier.TIER_0))
                .isInstanceOf(InsufficientTierException.class);
            assertThatThrownBy(() -> guard.validateMinimumTier(SecurityTier.TIER_2, SecurityTier.TIER_1))
                .isInstanceOf(InsufficientTierException.class);
        }
    }

    @Nested
    @DisplayName("TIER_0 required validation")
    class TierZeroRequiredValidation {

        @Test
        @DisplayName("should pass when actor is TIER_0")
        void shouldPassWhenActorIsTier0() {
            assertThatCode(() -> guard.validateTierZeroRequired(SecurityTier.TIER_0, "critical operation"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw when actor is TIER_1")
        void shouldThrowWhenActorIsTier1() {
            assertThatThrownBy(() -> guard.validateTierZeroRequired(SecurityTier.TIER_1, "critical operation"))
                .isInstanceOf(InsufficientTierException.class);
        }

        @Test
        @DisplayName("should throw when actor is TIER_2")
        void shouldThrowWhenActorIsTier2() {
            assertThatThrownBy(() -> guard.validateTierZeroRequired(SecurityTier.TIER_2, "critical operation"))
                .isInstanceOf(InsufficientTierException.class);
        }
    }

    @Nested
    @DisplayName("User management validation")
    class UserManagementValidation {

        @ParameterizedTest
        @CsvSource({
            "TIER_0, TIER_1, true",
            "TIER_0, TIER_2, true",
            "TIER_1, TIER_2, true",
            "TIER_0, TIER_0, false",
            "TIER_1, TIER_0, false",
            "TIER_1, TIER_1, false",
            "TIER_2, TIER_0, false",
            "TIER_2, TIER_1, false",
            "TIER_2, TIER_2, false"
        })
        @DisplayName("should validate user management based on tier hierarchy")
        void shouldValidateUserManagementBasedOnTierHierarchy(
                SecurityTier actorTier,
                SecurityTier targetTier,
                boolean shouldSucceed) {

            if (shouldSucceed) {
                assertThatCode(() -> guard.validateUserManagement(actorTier, targetTier))
                    .doesNotThrowAnyException();
            } else {
                assertThatThrownBy(() -> guard.validateUserManagement(actorTier, targetTier))
                    .isInstanceOf(PrivilegeEscalationException.class);
            }
        }

        @Test
        @DisplayName("TIER_0 cannot manage other TIER_0 users")
        void tier0CannotManageOtherTier0Users() {
            assertThatThrownBy(() -> guard.validateUserManagement(SecurityTier.TIER_0, SecurityTier.TIER_0))
                .isInstanceOf(PrivilegeEscalationException.class);
        }

        @Test
        @DisplayName("TIER_1 cannot manage TIER_0 or TIER_1 users")
        void tier1CannotManageHigherOrSameTierUsers() {
            assertThatThrownBy(() -> guard.validateUserManagement(SecurityTier.TIER_1, SecurityTier.TIER_0))
                .isInstanceOf(PrivilegeEscalationException.class);
            assertThatThrownBy(() -> guard.validateUserManagement(SecurityTier.TIER_1, SecurityTier.TIER_1))
                .isInstanceOf(PrivilegeEscalationException.class);
        }
    }

    @Nested
    @DisplayName("Complete security scenarios")
    class CompleteSecurityScenarios {

        @Test
        @DisplayName("SUPER_ADMIN cannot elevate another user to SUPER_ADMIN role")
        void superAdminCannotElevateToSuperAdmin() {
            Role superAdminRole = aRole().asSuperAdmin().build();

            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_0, superAdminRole))
                .isInstanceOf(PrivilegeEscalationException.class);
        }

        @Test
        @DisplayName("DIRECTOR cannot create or assign DIRECTOR role")
        void directorCannotCreateOrAssignDirectorRole() {
            Role directorRole = aRole().asDirector().build();

            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_1, directorRole))
                .isInstanceOf(PrivilegeEscalationException.class);
        }

        @Test
        @DisplayName("HR_MANAGER cannot perform any role assignments")
        void hrManagerCannotPerformAnyRoleAssignments() {
            Role tier2Role = aRole().withTier(SecurityTier.TIER_2).asNonSystem().build();

            assertThatThrownBy(() -> guard.validateRoleAssignment(SecurityTier.TIER_2, tier2Role))
                .isInstanceOf(PrivilegeEscalationException.class);
        }

        @Test
        @DisplayName("system roles are protected from deletion even by TIER_0")
        void systemRolesAreProtectedFromDeletionEvenByTier0() {
            Role superAdminRole = aRole().asSuperAdmin().build();
            Role directorRole = aRole().asDirector().build();
            Role hrManagerRole = aRole().asHrManager().build();

            assertThatThrownBy(() -> guard.validateRoleDeletion(superAdminRole))
                .isInstanceOf(SystemResourceProtectedException.class);
            assertThatThrownBy(() -> guard.validateRoleDeletion(directorRole))
                .isInstanceOf(SystemResourceProtectedException.class);
            assertThatThrownBy(() -> guard.validateRoleDeletion(hrManagerRole))
                .isInstanceOf(SystemResourceProtectedException.class);
        }
    }
}
