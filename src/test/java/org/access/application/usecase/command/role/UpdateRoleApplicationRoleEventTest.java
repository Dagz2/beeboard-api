package org.access.application.usecase.command.role;

import org.access.application.dto.command.role.UpdateRoleApplicationRoleCommand;
import org.access.application.service.AuditService;
import org.access.application.service.SecurityContextService;
import org.access.domain.event.RoleApplicationRoleChangedEvent;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleApplicationRoleChangedEvent emission in UpdateRoleApplicationRole use case.
 *
 * <p>These tests verify that the correct domain event is published when a role's
 * ApplicationRole is changed, including the set of all affected user IDs.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateRoleApplicationRole - Event Emission Tests")
class UpdateRoleApplicationRoleEventTest {

    @Mock
    RoleRepository roleRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PrivilegeEscalationGuard privilegeGuard;

    @Mock
    AuditService auditService;

    @Mock
    SecurityContextService securityContextService;

    @Mock
    EventPublisher eventPublisher;

    UpdateRoleApplicationRole useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateRoleApplicationRole(
            roleRepository,
            userRepository,
            privilegeGuard,
            auditService,
            securityContextService,
            eventPublisher
        );
    }

    @Test
    @DisplayName("Should publish RoleApplicationRoleChangedEvent when ApplicationRole is updated")
    void shouldPublishRoleApplicationRoleChangedEventWhenApplicationRoleIsUpdated() {
        // Given
        UUID roleId = UUID.randomUUID();
        String roleCode = "TEAM_LEAD";

        Role role = TestFixtures.aRole()
            .withId(TestFixtures.roleIdOf(roleId))
            .withCode(roleCode)
            .withApplicationRole(ApplicationRole.MEMBER)
            .build();

        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID user3Id = UUID.randomUUID();

        User user1 = TestFixtures.aUser().withId(TestFixtures.userIdOf(user1Id)).build();
        User user2 = TestFixtures.aUser().withId(TestFixtures.userIdOf(user2Id)).build();
        User user3 = TestFixtures.aUser().withId(TestFixtures.userIdOf(user3Id)).build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        UpdateRoleApplicationRoleCommand command = new UpdateRoleApplicationRoleCommand(
            roleId,
            ApplicationRole.MANAGER
        );

        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));
        when(userRepository.findByRoleId(any(RoleId.class)))
            .thenReturn(List.of(user1, user2, user3));

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<RoleApplicationRoleChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(RoleApplicationRoleChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        RoleApplicationRoleChangedEvent event = eventCaptor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.roleId()).isEqualTo(roleId);
        assertThat(event.roleCode()).isEqualTo(roleCode);
        assertThat(event.oldApplicationRole()).isEqualTo(ApplicationRole.MEMBER);
        assertThat(event.newApplicationRole()).isEqualTo(ApplicationRole.MANAGER);
        assertThat(event.affectedUserIds()).containsExactlyInAnyOrder(user1Id, user2Id, user3Id);
        assertThat(event.affectedUserCount()).isEqualTo(3);
        assertThat(event.changedBy()).isEqualTo(ctx.actorId().id());
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo("RoleApplicationRoleChanged");
        assertThat(event.aggregateId()).isEqualTo(roleId);
    }

    @Test
    @DisplayName("Should publish event with empty affectedUserIds when no users have the role")
    void shouldPublishEventWithEmptyAffectedUserIdsWhenNoUsersHaveTheRole() {
        // Given
        UUID roleId = UUID.randomUUID();
        Role role = TestFixtures.aRole()
            .withId(TestFixtures.roleIdOf(roleId))
            .withApplicationRole(ApplicationRole.MEMBER)
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        UpdateRoleApplicationRoleCommand command = new UpdateRoleApplicationRoleCommand(
            roleId,
            ApplicationRole.MANAGER
        );

        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));
        when(userRepository.findByRoleId(any(RoleId.class))).thenReturn(List.of());

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<RoleApplicationRoleChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(RoleApplicationRoleChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        RoleApplicationRoleChangedEvent event = eventCaptor.getValue();
        assertThat(event.affectedUserIds()).isEmpty();
        assertThat(event.affectedUserCount()).isZero();
    }

    @Test
    @DisplayName("Should publish event with escalation details when ApplicationRole is upgraded")
    void shouldPublishEventWithEscalationDetailsWhenApplicationRoleIsUpgraded() {
        // Given
        UUID roleId = UUID.randomUUID();
        Role role = TestFixtures.aRole()
            .withId(TestFixtures.roleIdOf(roleId))
            .withApplicationRole(ApplicationRole.MEMBER)
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        UpdateRoleApplicationRoleCommand command = new UpdateRoleApplicationRoleCommand(
            roleId,
            ApplicationRole.SUPERVISOR
        );

        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));
        when(userRepository.findByRoleId(any(RoleId.class))).thenReturn(List.of());

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<RoleApplicationRoleChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(RoleApplicationRoleChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        RoleApplicationRoleChangedEvent event = eventCaptor.getValue();
        assertThat(event.isEscalation()).isTrue();
        assertThat(event.isDegradation()).isFalse();
    }

    @Test
    @DisplayName("Should publish event with degradation details when ApplicationRole is downgraded")
    void shouldPublishEventWithDegradationDetailsWhenApplicationRoleIsDowngraded() {
        // Given
        UUID roleId = UUID.randomUUID();
        Role role = TestFixtures.aRole()
            .withId(TestFixtures.roleIdOf(roleId))
            .withApplicationRole(ApplicationRole.MANAGER)
            .build();

        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        UpdateRoleApplicationRoleCommand command = new UpdateRoleApplicationRoleCommand(
            roleId,
            ApplicationRole.GUEST
        );

        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));
        when(userRepository.findByRoleId(any(RoleId.class))).thenReturn(List.of());

        // When
        useCase.execute(command, ctx);

        // Then
        ArgumentCaptor<RoleApplicationRoleChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(RoleApplicationRoleChangedEvent.class);
        verify(eventPublisher).publishAsync(eventCaptor.capture());

        RoleApplicationRoleChangedEvent event = eventCaptor.getValue();
        assertThat(event.isDegradation()).isTrue();
        assertThat(event.isEscalation()).isFalse();
    }

    @Test
    @DisplayName("Should invalidate security context for affected users before publishing event")
    void shouldInvalidateSecurityContextForAffectedUsersBeforePublishingEvent() {
        // Given
        UUID roleId = UUID.randomUUID();
        Role role = TestFixtures.aRole().withId(TestFixtures.roleIdOf(roleId)).build();
        SecurityContext ctx = TestFixtures.aSecurityContext().asSuperAdmin().build();
        UpdateRoleApplicationRoleCommand command = new UpdateRoleApplicationRoleCommand(
            roleId,
            ApplicationRole.MANAGER
        );

        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));
        when(userRepository.findByRoleId(any(RoleId.class))).thenReturn(List.of());

        // When
        useCase.execute(command, ctx);

        // Then - verify cache invalidation happens before event publication
        verify(securityContextService).invalidateByRole(any(RoleId.class));
        verify(eventPublisher).publishAsync(any(RoleApplicationRoleChangedEvent.class));
    }
}
