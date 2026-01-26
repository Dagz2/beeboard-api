package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.UnlockUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.event.UserUnlockedEvent;
import org.access.domain.model.*;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.access.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.event.EventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserUnlockedEvent emission in UnlockUser use case.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UnlockUser - Event Emission Tests")
class UnlockUserEventTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PrivilegeEscalationGuard privilegeGuard;

    @Mock
    AuditService auditService;

    @Mock
    EventPublisher eventPublisher;

    UnlockUser useCase;

    @BeforeEach
    void setUp() {
        useCase = new UnlockUser(
            userRepository,
            roleRepository,
            privilegeGuard,
            auditService,
            eventPublisher
        );
    }

    @Test
    @DisplayName("Should publish UserUnlockedEvent when user is successfully unlocked")
    void shouldPublishUserUnlockedEventWhenUserIsUnlocked() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";

        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withEmail(email)
            .locked()
            .build();

        Role role = TestFixtures.aRole().build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        UnlockUserCommand command = new UnlockUserCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<UserUnlockedEvent> eventCaptor = ArgumentCaptor.forClass(UserUnlockedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserUnlockedEvent event = eventCaptor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.email()).isEqualTo(email);
        assertThat(event.unlockedBy()).isEqualTo(ctx.actorId().id());
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo("UserUnlocked");
        assertThat(event.aggregateId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should publish event after user is unlocked in repository")
    void shouldPublishEventAfterUserIsUnlockedInRepository() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestFixtures.aUser().withId(TestFixtures.userIdOf(userId)).locked().build();
        Role role = TestFixtures.aRole().build();
        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        UnlockUserCommand command = new UnlockUserCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then - verify event is published after update
        verify(userRepository).update(user);
        verify(eventPublisher).publishAsync(any(UserUnlockedEvent.class));
        verify(auditService).record(any(), any(), any(), any(), any(), any());
    }
}
