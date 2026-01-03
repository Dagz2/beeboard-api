package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.CreateUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.exception.PrivilegeEscalationException;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUser Use Case")
class CreateUserTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PrivilegeEscalationGuard privilegeGuard;

    @Mock
    AuditService auditService;

    CreateUser useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateUser(userRepository, roleRepository, privilegeGuard, auditService);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should create user with valid data")
        void shouldCreateUserWithValidData() {
            // Given
            Role targetRole = aRole()
                .withTier(SecurityTier.TIER_2)
                .build();
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(targetRole));
            doNothing().when(privilegeGuard).validateRoleAssignment(any(), any());

            CreateUserCommand command = new CreateUserCommand(
                "Alice",
                "Martin",
                "alice@example.com",
                VALID_PASSWORD,
                targetRole.id().id(),
                LocalDate.now().plusYears(1)
            );

            // When
            useCase.execute(command, ctx);

            // Then
            verify(userRepository).save(any(User.class));
            verify(auditService).record(eq(ctx), eq("CREATE_USER"), eq("USER"), any());
        }

        @Test
        @DisplayName("should reject when role not found")
        void shouldRejectWhenRoleNotFound() {
            // Given
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.empty());

            CreateUserCommand command = new CreateUserCommand(
                "Alice",
                "Martin",
                "alice@example.com",
                VALID_PASSWORD,
                randomRoleId().id(),
                LocalDate.now().plusYears(1)
            );

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
        }

        @Test
        @DisplayName("should reject privilege escalation")
        void shouldRejectPrivilegeEscalation() {
            // Given
            Role tier0Role = aRole()
                .asSuperAdmin()
                .build();
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(tier0Role));
            doThrow(new PrivilegeEscalationException("Cannot assign higher tier role"))
                .when(privilegeGuard).validateRoleAssignment(any(), any());

            CreateUserCommand command = new CreateUserCommand(
                "Alice",
                "Martin",
                "alice@example.com",
                VALID_PASSWORD,
                tier0Role.id().id(),
                LocalDate.now().plusYears(1)
            );

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command, ctx))
                .isInstanceOf(PrivilegeEscalationException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should audit user creation")
        void shouldAuditUserCreation() {
            // Given
            Role targetRole = aRole()
                .withTier(SecurityTier.TIER_2)
                .build();
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(targetRole));

            CreateUserCommand command = new CreateUserCommand(
                "Alice",
                "Martin",
                "alice@example.com",
                VALID_PASSWORD,
                targetRole.id().id(),
                LocalDate.now().plusYears(1)
            );

            // When
            useCase.execute(command, ctx);

            // Then
            verify(auditService).record(
                eq(ctx),
                eq("CREATE_USER"),
                eq("USER"),
                any()
            );
        }
    }
}
