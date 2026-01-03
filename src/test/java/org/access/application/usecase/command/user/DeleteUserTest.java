package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.DeleteUserCommand;
import org.access.application.service.AuditService;
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
@DisplayName("DeleteUser Use Case")
class DeleteUserTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PrivilegeEscalationGuard privilegeGuard;

    @Mock
    AuditService auditService;

    DeleteUser useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteUser(userRepository, roleRepository, privilegeGuard, auditService);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should delete locked user")
        void shouldDeleteLockedUser() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId roleId = randomRoleId();

            User user = aUser()
                .withId(targetUserId)
                .withRoleId(roleId)
                .locked()
                .build();

            Role targetRole = aRole()
                .withId(roleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(targetRole));

            DeleteUserCommand command = new DeleteUserCommand(targetUserId.id());

            // When
            useCase.execute(command, ctx);

            // Then
            verify(userRepository).deleteById(targetUserId);
        }

        @Test
        @DisplayName("should reject deleting unlocked user")
        void shouldRejectDeletingUnlockedUser() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId roleId = randomRoleId();

            User user = aUser()
                .withId(targetUserId)
                .withRoleId(roleId)
                .unlocked()
                .build();

            Role targetRole = aRole()
                .withId(roleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(targetRole));

            DeleteUserCommand command = new DeleteUserCommand(targetUserId.id());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User must be locked before deletion");

            verify(userRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should reject self-deletion")
        void shouldRejectSelfDeletion() {
            // Given
            UserId actorId = randomUserId();
            SecurityContext ctx = aSecurityContext()
                .withActorId(actorId)
                .asDirector()
                .build();

            doThrow(new SelfModificationForbiddenException("Cannot delete yourself"))
                .when(privilegeGuard).validateNotSelfModification(eq(actorId), eq(actorId));

            DeleteUserCommand command = new DeleteUserCommand(actorId.id());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(SelfModificationForbiddenException.class);

            verify(userRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should reject deleting higher tier user")
        void shouldRejectDeletingHigherTierUser() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId roleId = randomRoleId();

            User user = aUser()
                .withId(targetUserId)
                .withRoleId(roleId)
                .locked()
                .build();

            Role tier0Role = aRole()
                .withId(roleId)
                .asSuperAdmin()
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(tier0Role));
            doThrow(new PrivilegeEscalationException("Cannot delete higher tier user"))
                .when(privilegeGuard).validateUserManagement(any(), eq(SecurityTier.TIER_0));

            DeleteUserCommand command = new DeleteUserCommand(targetUserId.id());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(PrivilegeEscalationException.class);

            verify(userRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should reject when user not found")
        void shouldRejectWhenUserNotFound() {
            // Given
            UserId targetUserId = randomUserId();
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

            DeleteUserCommand command = new DeleteUserCommand(targetUserId.id());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should audit delete operation")
        void shouldAuditDeleteOperation() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId roleId = randomRoleId();

            User user = aUser()
                .withId(targetUserId)
                .withRoleId(roleId)
                .withEmail("deleted@example.com")
                .locked()
                .build();

            Role targetRole = aRole()
                .withId(roleId)
                .withTier(SecurityTier.TIER_2)
                .build();

            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(targetRole));

            DeleteUserCommand command = new DeleteUserCommand(targetUserId.id());

            // When
            useCase.execute(command, ctx);

            // Then
            verify(auditService).record(
                eq(ctx),
                eq("DELETE_USER"),
                eq("USER"),
                eq(targetUserId.id().toString()),
                contains("deleted@example.com"),
                isNull()
            );
        }
    }
}
