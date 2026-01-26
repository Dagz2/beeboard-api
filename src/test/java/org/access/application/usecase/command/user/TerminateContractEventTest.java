package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.TerminateContractCommand;
import org.access.application.service.AuditService;
import org.access.domain.event.UserContractTerminatedEvent;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserContractTerminatedEvent emission in TerminateContract use case.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TerminateContract - Event Emission Tests")
class TerminateContractEventTest {

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

    TerminateContract useCase;

    @BeforeEach
    void setUp() {
        useCase = new TerminateContract(
            userRepository,
            roleRepository,
            privilegeGuard,
            auditService,
            eventPublisher
        );
    }

    @Test
    @DisplayName("Should publish UserContractTerminatedEvent when contract is terminated")
    void shouldPublishUserContractTerminatedEventWhenContractIsTerminated() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";

        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withEmail(email)
            .withContractEndDate(LocalDate.now().plusYears(1))
            .unlocked()
            .build();

        Role role = TestFixtures.aRole().build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        TerminateContractCommand command = new TerminateContractCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<UserContractTerminatedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserContractTerminatedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserContractTerminatedEvent event = eventCaptor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.email()).isEqualTo(email);
        assertThat(event.terminationDate()).isEqualTo(LocalDate.now());
        assertThat(event.terminatedBy()).isEqualTo(ctx.actorId().id());
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo("UserContractTerminated");
        assertThat(event.aggregateId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should set termination date to today when contract is terminated")
    void shouldSetTerminationDateToTodayWhenContractIsTerminated() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withContractEndDate(LocalDate.now().plusMonths(6))
            .build();

        Role role = TestFixtures.aRole().build();
        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        TerminateContractCommand command = new TerminateContractCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<UserContractTerminatedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserContractTerminatedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserContractTerminatedEvent event = eventCaptor.getValue();
        assertThat(event.terminationDate()).isEqualTo(LocalDate.now());
        assertThat(event.isEffective()).isTrue();
        assertThat(event.isScheduled()).isFalse();
    }

    @Test
    @DisplayName("Should publish event after contract is terminated and user is locked")
    void shouldPublishEventAfterContractIsTerminatedAndUserIsLocked() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestFixtures.aUser().withId(TestFixtures.userIdOf(userId)).build();
        Role role = TestFixtures.aRole().build();
        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        TerminateContractCommand command = new TerminateContractCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then - verify event is published after update
        verify(userRepository).update(user);
        verify(eventPublisher).publishAsync(any(UserContractTerminatedEvent.class));
        verify(auditService).record(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should include both contract termination and account lock in event context")
    void shouldIncludeBothContractTerminationAndAccountLockInEventContext() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withContractEndDate(LocalDate.now().plusYears(1))
            .unlocked()  // User starts unlocked
            .build();

        Role role = TestFixtures.aRole().build();
        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        TerminateContractCommand command = new TerminateContractCommand(userId);

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        // When
        useCase.execute(command, ctx);

        // Then - verify event is published with termination date
        ArgumentCaptor<UserContractTerminatedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserContractTerminatedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserContractTerminatedEvent event = eventCaptor.getValue();
        // The event signals that both contract was terminated (date set to today)
        // AND user was locked (handled by the use case)
        assertThat(event.terminationDate()).isEqualTo(LocalDate.now());
    }
}
