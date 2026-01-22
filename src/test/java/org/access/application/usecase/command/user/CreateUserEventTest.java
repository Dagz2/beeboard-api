package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.CreateUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.event.UserCreatedEvent;
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
import org.shared.domain.AccountStatus;
import org.shared.domain.ApplicationRole;
import org.shared.event.EventPublisher;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying that {@link CreateUser} publishes {@link UserCreatedEvent}.
 *
 * <p>These tests ensure that newly created users trigger events for cross-BC synchronization.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUser - Event Publication Tests")
class CreateUserEventTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PrivilegeEscalationGuard privilegeGuard;

    @Mock
    private AuditService auditService;

    @Mock
    private EventPublisher eventPublisher;

    private CreateUser createUser;

    private SecurityContext superAdminContext;
    private Role memberRole;

    @BeforeEach
    void setUp() {
        createUser = new CreateUser(
            userRepository,
            roleRepository,
            privilegeGuard,
            auditService,
            eventPublisher
        );

        // Setup test data
        superAdminContext = TestFixtures.aSecurityContext()
            .asSuperAdmin()
            .build();

        memberRole = TestFixtures.aRole()
            .withCode("MEMBER")
            .withName("Member")
            .withApplicationRole(ApplicationRole.MEMBER)
            .withTier(SecurityTier.TIER_2)
            .build();
    }

    @Test
    @DisplayName("Should publish UserCreatedEvent when user is created successfully")
    void shouldPublishUserCreatedEventWhenUserIsCreated() {
        // Given
        CreateUserCommand command = new CreateUserCommand(
            "John",
            "Doe",
            "john.doe@example.com",
            "SecurePassword123!",
            memberRole.id().id(),
            LocalDate.now().plusYears(1)
        );

        when(roleRepository.findById(any(RoleId.class)))
            .thenReturn(Optional.of(memberRole));

        doNothing().when(privilegeGuard)
            .validateRoleAssignment(any(SecurityTier.class), any(Role.class));

        doNothing().when(userRepository).save(any(User.class));

        // When
        createUser.execute(command, superAdminContext);

        // Then - Verify event was published
        ArgumentCaptor<UserCreatedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        UserCreatedEvent event = eventCaptor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.userId()).isNotNull();
        assertThat(event.fullName()).isEqualTo("John Doe");
        assertThat(event.applicationRole()).isEqualTo(ApplicationRole.MEMBER);
        assertThat(event.accountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(event.createdBy()).isEqualTo(superAdminContext.actorId().id());
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("Should publish event with correct ApplicationRole from assigned role")
    void shouldPublishEventWithCorrectApplicationRoleFromAssignedRole() {
        // Given - Create user with ADMIN role
        Role adminRole = TestFixtures.aRole()
            .withCode("ADMIN")
            .withName("Administrator")
            .withApplicationRole(ApplicationRole.ADMIN)
            .withTier(SecurityTier.TIER_1)
            .build();

        CreateUserCommand command = new CreateUserCommand(
            "Admin",
            "User",
            "admin@example.com",
            "AdminPassword123!",
            adminRole.id().id(),
            LocalDate.now().plusYears(1)
        );

        when(roleRepository.findById(any(RoleId.class)))
            .thenReturn(Optional.of(adminRole));

        doNothing().when(userRepository).save(any(User.class));

        // When
        createUser.execute(command, superAdminContext);

        // Then - Event should contain ADMIN application role
        ArgumentCaptor<UserCreatedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        UserCreatedEvent event = eventCaptor.getValue();
        assertThat(event.applicationRole()).isEqualTo(ApplicationRole.ADMIN);
    }

    @Test
    @DisplayName("Should publish event with ACTIVE account status for new user")
    void shouldPublishEventWithActiveAccountStatusForNewUser() {
        // Given
        CreateUserCommand command = new CreateUserCommand(
            "Jane",
            "Smith",
            "jane.smith@example.com",
            "Password123!",
            memberRole.id().id(),
            LocalDate.now().plusYears(1)
        );

        when(roleRepository.findById(any(RoleId.class)))
            .thenReturn(Optional.of(memberRole));

        doNothing().when(userRepository).save(any(User.class));

        // When
        createUser.execute(command, superAdminContext);

        // Then - New users should have ACTIVE status
        ArgumentCaptor<UserCreatedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        UserCreatedEvent event = eventCaptor.getValue();
        assertThat(event.accountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should publish event with correct actor context")
    void shouldPublishEventWithCorrectActorContext() {
        // Given - Different actor creates the user
        UUID actorId = UUID.randomUUID();
        SecurityContext customContext = TestFixtures.aSecurityContext()
            .withActorId(new UserId(actorId))
            .withTier(SecurityTier.TIER_1)
            .build();

        CreateUserCommand command = new CreateUserCommand(
            "Test",
            "User",
            "test@example.com",
            "Password123!",
            memberRole.id().id(),
            LocalDate.now().plusYears(1)
        );

        when(roleRepository.findById(any(RoleId.class)))
            .thenReturn(Optional.of(memberRole));

        doNothing().when(userRepository).save(any(User.class));

        // When
        createUser.execute(command, customContext);

        // Then - Event should contain the correct actor ID
        ArgumentCaptor<UserCreatedEvent> eventCaptor =
            ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        UserCreatedEvent event = eventCaptor.getValue();
        assertThat(event.createdBy()).isEqualTo(actorId);
    }

    @Test
    @DisplayName("Should publish event only once per user creation")
    void shouldPublishEventOnlyOncePerUserCreation() {
        // Given
        CreateUserCommand command = new CreateUserCommand(
            "Single",
            "Event",
            "single@example.com",
            "Password123!",
            memberRole.id().id(),
            LocalDate.now().plusYears(1)
        );

        when(roleRepository.findById(any(RoleId.class)))
            .thenReturn(Optional.of(memberRole));

        doNothing().when(userRepository).save(any(User.class));

        // When
        createUser.execute(command, superAdminContext);

        // Then - Exactly one event should be published
        verify(eventPublisher, times(1)).publish(any(UserCreatedEvent.class));
    }
}
