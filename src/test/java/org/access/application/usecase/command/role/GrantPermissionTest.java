package org.access.application.usecase.command.role;

import org.access.application.service.AuditService;
import org.access.application.service.SecurityContextService;
import org.access.domain.exception.PrivilegeEscalationException;
import org.access.domain.exception.SystemResourceProtectedException;
import org.access.domain.model.*;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GrantPermission Use Case")
class GrantPermissionTest {

    @Mock
    RoleRepository roleRepository;

    @Mock
    PermissionRepository permissionRepository;

    @Mock
    PrivilegeEscalationGuard privilegeGuard;

    @Mock
    AuditService auditService;

    @Mock
    SecurityContextService securityContextService;

    GrantPermission useCase;

    @BeforeEach
    void setUp() {
        useCase = new GrantPermission(
            roleRepository,
            permissionRepository,
            privilegeGuard,
            auditService,
            securityContextService
        );
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should grant permission to role")
        void shouldGrantPermissionToRole() {
            // Given
            RoleId roleId = randomRoleId();
            PermissionId permId = randomPermissionId();

            Role role = aRole()
                .withId(roleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            Permission permission = aPermission()
                .withId(permId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permId)).thenReturn(Optional.of(permission));

            // When
            useCase.execute(roleId, permId, ctx);

            // Then
            assertThat(role.hasPermission(permId)).isTrue();
            verify(roleRepository).save(role);
        }

        @Test
        @DisplayName("should reject when role not found")
        void shouldRejectWhenRoleNotFound() {
            // Given
            RoleId roleId = randomRoleId();
            PermissionId permId = randomPermissionId();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(roleId, permId, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
        }

        @Test
        @DisplayName("should reject when permission not found")
        void shouldRejectWhenPermissionNotFound() {
            // Given
            RoleId roleId = randomRoleId();
            PermissionId permId = randomPermissionId();

            Role role = aRole()
                .withId(roleId)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(roleId, permId, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission not found");
        }

        @Test
        @DisplayName("should reject modifying system role by non-TIER_0")
        void shouldRejectModifyingSystemRoleByNonTier0() {
            // Given
            RoleId roleId = randomRoleId();
            PermissionId permId = randomPermissionId();

            Role systemRole = aRole()
                .withId(roleId)
                .asSuperAdmin()
                .build();

            Permission permission = aPermission()
                .withId(permId)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(roleId)).thenReturn(Optional.of(systemRole));
            when(permissionRepository.findById(permId)).thenReturn(Optional.of(permission));
            doThrow(new SystemResourceProtectedException("System role cannot be modified"))
                .when(privilegeGuard).validateSystemRoleModification(any(), any());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(roleId, permId, ctx))
                .isInstanceOf(SystemResourceProtectedException.class);

            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject granting higher tier permission")
        void shouldRejectGrantingHigherTierPermission() {
            // Given
            RoleId roleId = randomRoleId();
            PermissionId permId = randomPermissionId();

            Role role = aRole()
                .withId(roleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            Permission tier0Perm = aPermission()
                .withId(permId)
                .withTier(SecurityTier.TIER_0)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permId)).thenReturn(Optional.of(tier0Perm));
            doThrow(new PrivilegeEscalationException("Cannot grant higher tier permission"))
                .when(privilegeGuard).validatePermissionGrant(any(), any());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(roleId, permId, ctx))
                .isInstanceOf(PrivilegeEscalationException.class);

            verify(roleRepository, never()).save(any());
        }

        @Test
        @DisplayName("should invalidate security context for role users")
        void shouldInvalidateSecurityContextForRoleUsers() {
            // Given
            RoleId roleId = randomRoleId();
            PermissionId permId = randomPermissionId();

            Role role = aRole()
                .withId(roleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            Permission permission = aPermission()
                .withId(permId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permId)).thenReturn(Optional.of(permission));

            // When
            useCase.execute(roleId, permId, ctx);

            // Then
            verify(securityContextService).invalidateByRole(roleId);
        }

        @Test
        @DisplayName("should audit permission grant")
        void shouldAuditPermissionGrant() {
            // Given
            RoleId roleId = randomRoleId();
            PermissionId permId = randomPermissionId();

            Role role = aRole()
                .withId(roleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            Permission permission = aPermission()
                .withId(permId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permId)).thenReturn(Optional.of(permission));

            // When
            useCase.execute(roleId, permId, ctx);

            // Then
            verify(auditService).record(
                eq(ctx),
                eq("GRANT_PERMISSION"),
                eq("ROLE"),
                eq(roleId.id().toString()),
                isNull(),
                contains(permId.id().toString())
            );
        }
    }
}
