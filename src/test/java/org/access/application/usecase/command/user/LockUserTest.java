package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.LockUserCommand;
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
@DisplayName("LockUser Use Case")
class LockUserTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PrivilegeEscalationGuard privilegeGuard;

    @Mock
    AuditService auditService;

    @Mock
    org.shared.event.EventPublisher eventPublisher;

    LockUser useCase;

    @BeforeEach
    void setUp() {
        useCase = new LockUser(userRepository, roleRepository, privilegeGuard, auditService, eventPublisher);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should lock unlocked user")
        void shouldLockUnlockedUser() {
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

            LockUserCommand command = new LockUserCommand(targetUserId.id());

            // When
            useCase.execute(command, ctx);

            // Then
            assertThat(user.isLocked()).isTrue();
            verify(userRepository).update(user);
        }

        @Test
        @DisplayName("should reject self-locking")
        void shouldRejectSelfLocking() {
            // Given
            UserId actorId = randomUserId();
            SecurityContext ctx = aSecurityContext()
                .withActorId(actorId)
                .asDirector()
                .build();

            doThrow(new SelfModificationForbiddenException("Cannot lock yourself"))
                .when(privilegeGuard).validateNotSelfModification(eq(actorId), eq(actorId));

            LockUserCommand command = new LockUserCommand(actorId.id());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(SelfModificationForbiddenException.class);

            verify(userRepository, never()).update(any());
        }

        @Test
        @DisplayName("should reject locking higher tier user")
        void shouldRejectLockingHigherTierUser() {
            // Given
            UserId targetUserId = randomUserId();
            RoleId roleId = randomRoleId();

            User user = aUser()
                .withId(targetUserId)
                .withRoleId(roleId)
                .unlocked()
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
            doThrow(new PrivilegeEscalationException("Cannot lock higher tier user"))
                .when(privilegeGuard).validateUserManagement(any(), eq(SecurityTier.TIER_0));

            LockUserCommand command = new LockUserCommand(targetUserId.id());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(PrivilegeEscalationException.class);

            verify(userRepository, never()).update(any());
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

            LockUserCommand command = new LockUserCommand(targetUserId.id());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should audit lock operation")
        void shouldAuditLockOperation() {
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

            LockUserCommand command = new LockUserCommand(targetUserId.id());

            // When
            useCase.execute(command, ctx);

            // Then
            verify(auditService).record(
                eq(ctx),
                eq("LOCK_USER"),
                eq("USER"),
                eq(targetUserId.id().toString()),
                eq("{\"isLocked\": false}"),
                eq("{\"isLocked\": true}")
            );
        }
    }
}
