package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.ChangeFullNameCommand;
import org.access.domain.event.UserFullNameChangedEvent;
import org.access.domain.model.FullName;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;
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
 * Unit tests for UserFullNameChangedEvent emission in ChangeFullName use case.
 *
 * <p>These tests verify that name changes trigger events for cross-BC synchronization
 * (specifically, updating the Note BC author_view table).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeFullName - Event Emission Tests")
class ChangeFullNameEventTest {

    @Mock
    UserRepository userRepository;

    @Mock
    EventPublisher eventPublisher;

    ChangeFullName useCase;

    @BeforeEach
    void setUp() {
        useCase = new ChangeFullName(userRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should publish UserFullNameChangedEvent when user name is changed")
    void shouldPublishUserFullNameChangedEventWhenNameIsChanged() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        // User starts with name "Jane Doe"
        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withFullName(new FullName("Jane", "Doe"))
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext()
            .withActorId(new UserId(actorId))
            .asSuperAdmin()
            .build();

        ChangeFullNameCommand command = new ChangeFullNameCommand(
            userId,
            "Janet",
            "Smith"
        );

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));

        // When
        useCase.execute(command, ctx);

        // Then - Verify event was published
        ArgumentCaptor<UserFullNameChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserFullNameChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserFullNameChangedEvent event = eventCaptor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.oldFirstName()).isEqualTo("Jane");
        assertThat(event.oldLastName()).isEqualTo("Doe");
        assertThat(event.newFirstName()).isEqualTo("Janet");
        assertThat(event.newLastName()).isEqualTo("Smith");
        assertThat(event.oldFullName()).isEqualTo("Jane Doe");
        assertThat(event.newFullName()).isEqualTo("Janet Smith");
        assertThat(event.changedBy()).isEqualTo(actorId);
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo("UserFullNameChanged");
        assertThat(event.aggregateId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should publish event after user is updated in repository")
    void shouldPublishEventAfterUserIsUpdatedInRepository() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withFullName(new FullName("John", "Smith"))
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();

        ChangeFullNameCommand command = new ChangeFullNameCommand(
            userId,
            "Jonathan",
            "Smith"
        );

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));

        // When
        useCase.execute(command, ctx);

        // Then - Verify event is published after update
        verify(userRepository).update(user);
        verify(eventPublisher).publishAsync(any(UserFullNameChangedEvent.class));
    }

    @Test
    @DisplayName("Should publish event with correct actor context")
    void shouldPublishEventWithCorrectActorContext() {
        // Given - Different actor changes the user's name
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withFullName(new FullName("Test", "User"))
            .build();

        SecurityContext customContext = TestFixtures.aSecurityContext()
            .withActorId(new UserId(actorId))
            .asSuperAdmin()
            .build();

        ChangeFullNameCommand command = new ChangeFullNameCommand(
            userId,
            "Updated",
            "Name"
        );

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));

        // When
        useCase.execute(command, customContext);

        // Then - Event should contain the correct actor ID
        ArgumentCaptor<UserFullNameChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserFullNameChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserFullNameChangedEvent event = eventCaptor.getValue();
        assertThat(event.changedBy()).isEqualTo(actorId);
    }

    @Test
    @DisplayName("Should capture old name before applying change")
    void shouldCaptureOldNameBeforeApplyingChange() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withFullName(new FullName("Original", "Name"))
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();

        ChangeFullNameCommand command = new ChangeFullNameCommand(
            userId,
            "Changed",
            "Name"
        );

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));

        // When
        useCase.execute(command, ctx);

        // Then - Event should contain the original name, not the changed name
        ArgumentCaptor<UserFullNameChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserFullNameChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserFullNameChangedEvent event = eventCaptor.getValue();
        assertThat(event.oldFirstName()).isEqualTo("Original");
        assertThat(event.oldLastName()).isEqualTo("Name");
        assertThat(event.newFirstName()).isEqualTo("Changed");
        assertThat(event.newLastName()).isEqualTo("Name");
    }

    @Test
    @DisplayName("Should publish event only once per name change")
    void shouldPublishEventOnlyOncePerNameChange() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withFullName(new FullName("Single", "Event"))
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();

        ChangeFullNameCommand command = new ChangeFullNameCommand(
            userId,
            "New",
            "Name"
        );

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));

        // When
        useCase.execute(command, ctx);

        // Then - Exactly one event should be published
        verify(eventPublisher, times(1)).publishAsync(any(UserFullNameChangedEvent.class));
    }
}
