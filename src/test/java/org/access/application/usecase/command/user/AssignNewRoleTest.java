package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.AssignNewRoleCommand;
import org.access.application.service.AuditService;
import org.access.application.service.SecurityContextService;
import org.access.domain.exception.PrivilegeEscalationException;
import org.access.domain.exception.SelfModificationForbiddenException;
import org.access.domain.model.*;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
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
@DisplayName("AssignNewRole Use Case")
class AssignNewRoleTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PrivilegeEscalationGuard privilegeGuard;

    @Mock
    AuditService auditService;

    @Mock
    SecurityContextService securityContextService;

    @Mock
    org.shared.event.EventPublisher eventPublisher;

    AssignNewRole useCase;

    @BeforeEach
    void setUp() {
        useCase = new AssignNewRole(
            userRepository,
            roleRepository,
            privilegeGuard,
            auditService,
            securityContextService,
            eventPublisher
        );
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should assign new role to user")
        void shouldAssignNewRoleToUser() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId oldRoleId = randomRoleId();
            RoleId newRoleId = randomRoleId();

            User user = aUser()
                .withId(targetUserId)
                .withRoleId(oldRoleId)
                .build();

            Role oldRole = aRole()
                .withId(oldRoleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            Role newRole = aRole()
                .withId(newRoleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(oldRoleId)).thenReturn(Optional.of(oldRole));
            when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));
            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));

            AssignNewRoleCommand command = new AssignNewRoleCommand(
                targetUserId.id(),
                newRoleId.id()
            );

            // When
            useCase.execute(command, ctx);

            // Then
            assertThat(user.getRoleId()).isEqualTo(newRoleId);
            verify(userRepository).update(user);
            verify(securityContextService).invalidate(targetUserId.id());
        }

        @Test
        @DisplayName("should reject self-modification")
        void shouldRejectSelfModification() {
            // Given
            UserId actorId = randomUserId();
            RoleId newRoleId = randomRoleId();

            SecurityContext ctx = aSecurityContext()
                .withActorId(actorId)
                .asDirector()
                .build();

            doThrow(new SelfModificationForbiddenException("Cannot modify own role"))
                .when(privilegeGuard).validateNotSelfModification(eq(actorId), eq(actorId));

            AssignNewRoleCommand command = new AssignNewRoleCommand(
                actorId.id(),
                newRoleId.id()
            );

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(SelfModificationForbiddenException.class);

            verify(userRepository, never()).update(any());
        }

        @Test
        @DisplayName("should reject privilege escalation")
        void shouldRejectPrivilegeEscalation() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId newRoleId = randomRoleId();

            Role tier0Role = aRole()
                .withId(newRoleId)
                .asSuperAdmin()
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(tier0Role));
            doThrow(new PrivilegeEscalationException("Cannot assign higher tier role"))
                .when(privilegeGuard).validateRoleAssignment(any(), any());

            AssignNewRoleCommand command = new AssignNewRoleCommand(
                targetUserId.id(),
                newRoleId.id()
            );

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(PrivilegeEscalationException.class);

            verify(userRepository, never()).update(any());
        }

        @Test
        @DisplayName("should reject when role not found")
        void shouldRejectWhenRoleNotFound() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId newRoleId = randomRoleId();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(newRoleId)).thenReturn(Optional.empty());

            AssignNewRoleCommand command = new AssignNewRoleCommand(
                targetUserId.id(),
                newRoleId.id()
            );

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
        }

        @Test
        @DisplayName("should reject when user not found")
        void shouldRejectWhenUserNotFound() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId newRoleId = randomRoleId();

            Role newRole = aRole()
                .withId(newRoleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));
            when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

            AssignNewRoleCommand command = new AssignNewRoleCommand(
                targetUserId.id(),
                newRoleId.id()
            );

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should invalidate security context cache after role change")
        void shouldInvalidateSecurityContextCache() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId oldRoleId = randomRoleId();
            RoleId newRoleId = randomRoleId();

            User user = aUser()
                .withId(targetUserId)
                .withRoleId(oldRoleId)
                .build();

            Role oldRole = aRole()
                .withId(oldRoleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            Role newRole = aRole()
                .withId(newRoleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(oldRoleId)).thenReturn(Optional.of(oldRole));
            when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));
            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));

            AssignNewRoleCommand command = new AssignNewRoleCommand(
                targetUserId.id(),
                newRoleId.id()
            );

            // When
            useCase.execute(command, ctx);

            // Then
            verify(securityContextService).invalidate(targetUserId.id());
        }

        @Test
        @DisplayName("should audit role assignment")
        void shouldAuditRoleAssignment() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId oldRoleId = randomRoleId();
            RoleId newRoleId = randomRoleId();

            User user = aUser()
                .withId(targetUserId)
                .withRoleId(oldRoleId)
                .build();

            Role oldRole = aRole()
                .withId(oldRoleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            Role newRole = aRole()
                .withId(newRoleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(oldRoleId)).thenReturn(Optional.of(oldRole));
            when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));
            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));

            AssignNewRoleCommand command = new AssignNewRoleCommand(
                targetUserId.id(),
                newRoleId.id()
            );

            // When
            useCase.execute(command, ctx);

            // Then
            verify(auditService).record(
                eq(ctx),
                eq("ASSIGN_ROLE"),
                eq("USER"),
                eq(targetUserId.id().toString()),
                contains(oldRoleId.id().toString()),
                contains(newRoleId.id().toString())
            );
        }
    }
}
