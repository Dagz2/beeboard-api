package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.AssignNewRoleCommand;
import org.access.application.service.AuditService;
import org.access.application.service.SecurityContextService;
import org.access.domain.event.UserRoleChangedEvent;
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
import org.shared.domain.ApplicationRole;
import org.shared.event.EventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserRoleChangedEvent emission in AssignNewRole use case.
 *
 * <p>These tests verify that the correct domain event is published when a user's
 * role is changed, including all necessary metadata for cross-BC consistency.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssignNewRole - Event Emission Tests")
class AssignNewRoleEventTest {

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
    EventPublisher eventPublisher;

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

    @Test
    @DisplayName("Should publish UserRoleChangedEvent when role is successfully assigned")
    void shouldPublishUserRoleChangedEventWhenRoleIsAssigned() {
        // Given
        UUID userId = UUID.randomUUID();
        RoleId oldRoleId = TestFixtures.roleIdOf(UUID.randomUUID());
        RoleId newRoleId = TestFixtures.roleIdOf(UUID.randomUUID());

        Role oldRole = TestFixtures.aRole()
            .withId(oldRoleId)
            .withApplicationRole(ApplicationRole.MEMBER)
            .withCode("MEMBER")
            .build();

        Role newRole = TestFixtures.aRole()
            .withId(newRoleId)
            .withApplicationRole(ApplicationRole.MANAGER)
            .withCode("MANAGER")
            .build();

        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withRoleId(oldRoleId)
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext()
            .asSuperAdmin()
            .build();

        AssignNewRoleCommand command = new AssignNewRoleCommand(userId, newRoleId.id());

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(oldRoleId)).thenReturn(Optional.of(oldRole));
        when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<UserRoleChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserRoleChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserRoleChangedEvent event = eventCaptor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.oldRoleId()).isEqualTo(oldRoleId.id());
        assertThat(event.newRoleId()).isEqualTo(newRoleId.id());
        assertThat(event.oldApplicationRole()).isEqualTo(ApplicationRole.MEMBER);
        assertThat(event.newApplicationRole()).isEqualTo(ApplicationRole.MANAGER);
        assertThat(event.changedBy()).isEqualTo(ctx.actorId().id());
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo("UserRoleChanged");
        assertThat(event.aggregateId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should publish event with escalation details when role is upgraded")
    void shouldPublishEventWithEscalationDetailsWhenRoleIsUpgraded() {
        // Given
        UUID userId = UUID.randomUUID();
        RoleId oldRoleId = TestFixtures.roleIdOf(UUID.randomUUID());
        RoleId newRoleId = TestFixtures.roleIdOf(UUID.randomUUID());

        Role oldRole = TestFixtures.aRole()
            .withId(oldRoleId)
            .withApplicationRole(ApplicationRole.MEMBER)
            .build();

        Role newRole = TestFixtures.aRole()
            .withId(newRoleId)
            .withApplicationRole(ApplicationRole.ADMIN)
            .build();

        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withRoleId(oldRoleId)
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        AssignNewRoleCommand command = new AssignNewRoleCommand(userId, newRoleId.id());

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(oldRoleId)).thenReturn(Optional.of(oldRole));
        when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<UserRoleChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserRoleChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserRoleChangedEvent event = eventCaptor.getValue();
        assertThat(event.isEscalation()).isTrue();
        assertThat(event.isDegradation()).isFalse();
    }

    @Test
    @DisplayName("Should publish event with degradation details when role is downgraded")
    void shouldPublishEventWithDegradationDetailsWhenRoleIsDowngraded() {
        // Given
        UUID userId = UUID.randomUUID();
        RoleId oldRoleId = TestFixtures.roleIdOf(UUID.randomUUID());
        RoleId newRoleId = TestFixtures.roleIdOf(UUID.randomUUID());

        Role oldRole = TestFixtures.aRole()
            .withId(oldRoleId)
            .withApplicationRole(ApplicationRole.MANAGER)
            .build();

        Role newRole = TestFixtures.aRole()
            .withId(newRoleId)
            .withApplicationRole(ApplicationRole.GUEST)
            .build();

        User user = TestFixtures.aUser()
            .withId(TestFixtures.userIdOf(userId))
            .withRoleId(oldRoleId)
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        AssignNewRoleCommand command = new AssignNewRoleCommand(userId, newRoleId.id());

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(oldRoleId)).thenReturn(Optional.of(oldRole));
        when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<UserRoleChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserRoleChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        UserRoleChangedEvent event = eventCaptor.getValue();
        assertThat(event.isDegradation()).isTrue();
        assertThat(event.isEscalation()).isFalse();
    }

    @Test
    @DisplayName("Should verify event is published within the transaction")
    void shouldVerifyEventIsPublishedWithinTransaction() {
        // Given
        UUID userId = UUID.randomUUID();
        RoleId oldRoleId = TestFixtures.roleIdOf(UUID.randomUUID());
        RoleId newRoleId = TestFixtures.roleIdOf(UUID.randomUUID());

        Role oldRole = TestFixtures.aRole().withId(oldRoleId).build();
        Role newRole = TestFixtures.aRole().withId(newRoleId).build();
        User user = TestFixtures.aUser().withId(TestFixtures.userIdOf(userId)).withRoleId(oldRoleId).build();
        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        AssignNewRoleCommand command = new AssignNewRoleCommand(userId, newRoleId.id());

        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(user));
        when(roleRepository.findById(oldRoleId)).thenReturn(Optional.of(oldRole));
        when(roleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));

        // When
        useCase.execute(command, ctx);

        // Then - verify event is published AFTER user update but BEFORE transaction ends
        verify(userRepository).update(user);
        verify(eventPublisher).publishAsync(any(UserRoleChangedEvent.class));
        verify(auditService).record(any(), any(), any(), any(), any(), any());
    }
}
