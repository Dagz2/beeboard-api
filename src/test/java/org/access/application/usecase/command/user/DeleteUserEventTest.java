package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.DeleteUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.event.UserDeletedEvent;
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
 * Unit tests for UserDeletedEvent emission in DeleteUser use case.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUser - Event Emission Tests")
class DeleteUserEventTest {

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

    DeleteUser useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteUser(
            userRepository,
            roleRepository,
            privilegeGuard,
            auditService,
            eventPublisher
        );
    }

    @Test
    @DisplayName("Should publish UserDeletedEvent when user is successfully deleted")
    void shouldPublishUserDeletedEventWhenUserIsDeleted() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String fullName = "John Doe";

        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withEmail(email)
            .withFullName("John", "Doe")
            .locked()  // User must be locked before deletion
            .build();

        Role role = TestFixtures.aRole().build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        DeleteUserCommand command = new DeleteUserCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<UserDeletedEvent> eventCaptor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        UserDeletedEvent event = eventCaptor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.email()).isEqualTo(email);
        assertThat(event.fullName()).isEqualTo(fullName);
        assertThat(event.deletedBy()).isEqualTo(ctx.actorId().id());
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo("UserDeleted");
        assertThat(event.aggregateId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should capture user data before deletion for event payload")
    void shouldCaptureUserDataBeforeDeletionForEventPayload() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "deleted@example.com";
        String firstName = "Jane";
        String lastName = "Smith";

        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withEmail(email)
            .withFullName(firstName, lastName)
            .locked()
            .build();

        Role role = TestFixtures.aRole().build();
        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        DeleteUserCommand command = new DeleteUserCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then - verify user data is captured BEFORE deletion
        ArgumentCaptor<UserDeletedEvent> eventCaptor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        UserDeletedEvent event = eventCaptor.getValue();
        assertThat(event.email()).isEqualTo(email);
        assertThat(event.fullName()).isEqualTo(firstName + " " + lastName);
    }

    @Test
    @DisplayName("Should publish event after user is deleted from repository")
    void shouldPublishEventAfterUserIsDeletedFromRepository() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestFixtures.aUser().withId(TestFixtures.userIdOf(userId)).locked().build();
        Role role = TestFixtures.aRole().build();
        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        DeleteUserCommand command = new DeleteUserCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then - verify event is published after deletion
        verify(userRepository).deleteById(any(UserId.class));
        verify(eventPublisher).publish(any(UserDeletedEvent.class));
        verify(auditService).record(any(), any(), any(), any(), any(), any());
    }
}
