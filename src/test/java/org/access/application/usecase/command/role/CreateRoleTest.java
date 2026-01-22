package org.access.application.usecase.command.role;

import org.access.application.dto.command.role.CreateRoleCommand;
import org.access.application.service.AuditService;
import org.access.domain.model.*;
import org.access.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.domain.ApplicationRole;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateRole Use Case")
class CreateRoleTest {

    @Mock
    RoleRepository roleRepository;

    @Mock
    AuditService auditService;

    CreateRole useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateRole(roleRepository, auditService);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should create role with valid data")
        void shouldCreateRoleWithValidData() {
            // Given
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            CreateRoleCommand command = new CreateRoleCommand("NEW_ROLE", "New Role", SecurityTier.TIER_2, false, ApplicationRole.MEMBER);

            // When
            useCase.execute(command, ctx);

            // Then
            ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(roleCaptor.capture());

            Role savedRole = roleCaptor.getValue();
            assertThat(savedRole.code().value()).isEqualTo("NEW_ROLE");
            assertThat(savedRole.name().value()).isEqualTo("New Role");
        }

        @Test
        @DisplayName("should create role with TIER_2 by default")
        void shouldCreateRoleWithDefaultTier() {
            // Given
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            CreateRoleCommand command = new CreateRoleCommand("BASIC_ROLE", "Basic Role", SecurityTier.TIER_2, false, ApplicationRole.MEMBER);

            // When
            useCase.execute(command, ctx);

            // Then
            ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(roleCaptor.capture());

            Role savedRole = roleCaptor.getValue();
            assertThat(savedRole.tier()).isEqualTo(SecurityTier.TIER_2);
        }

        @Test
        @DisplayName("should create role as non-system by default")
        void shouldCreateRoleAsNonSystem() {
            // Given
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            CreateRoleCommand command = new CreateRoleCommand("CUSTOM_ROLE", "Custom Role", SecurityTier.TIER_2, false, ApplicationRole.MEMBER);

            // When
            useCase.execute(command, ctx);

            // Then
            ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(roleCaptor.capture());

            Role savedRole = roleCaptor.getValue();
            assertThat(savedRole.isSystem()).isFalse();
        }

        @Test
        @DisplayName("should audit role creation")
        void shouldAuditRoleCreation() {
            // Given
            SecurityContext ctx = aSecurityContext()
                .asDirector()
                .build();

            CreateRoleCommand command = new CreateRoleCommand("AUDITED_ROLE", "Audited Role", SecurityTier.TIER_2, false, ApplicationRole.MEMBER);

            // When
            useCase.execute(command, ctx);

            // Then
            verify(auditService).record(
                eq(ctx),
                eq("CREATE_ROLE"),
                eq("ROLE"),
                any(),
                isNull(),
                contains("AUDITED_ROLE")
            );
        }
    }
}
