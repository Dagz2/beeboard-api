package org.access.infrastructure.repository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.domain.model.*;
import org.access.domain.repository.RoleRepository;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@QuarkusTest
@DisplayName("RoleRepository Integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleRepositoryIT {

    @Inject
    RoleRepository roleRepository;

    static RoleId testRoleId;

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should save and retrieve role")
        void shouldSaveAndRetrieveRole() {
            // Given
            Role role = aRole()
                .withCode("TEST_ROLE_IT")
                .withName("Integration Test Role")
                .withTier(SecurityTier.TIER_2)
                .build();
            testRoleId = role.id();

            // When
            roleRepository.save(role);
            Optional<Role> found = roleRepository.findById(testRoleId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().code().value()).isEqualTo("TEST_ROLE_IT");
            assertThat(found.get().name().value()).isEqualTo("Integration Test Role");
            assertThat(found.get().tier()).isEqualTo(SecurityTier.TIER_2);
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should find role by code")
        void shouldFindRoleByCode() {
            // Given - role created in previous test
            RoleCode code = new RoleCode("TEST_ROLE_IT");

            // When
            Optional<Role> found = roleRepository.findByCode(code);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().code()).isEqualTo(code);
        }

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should update existing role")
        void shouldUpdateRole() {
            // Given
            Optional<Role> roleOpt = roleRepository.findById(testRoleId);
            assertThat(roleOpt).isPresent();
            Role role = roleOpt.get();

            // When - update the name
            Role updated = new Role(
                role.id(),
                role.code(),
                new RoleName("Updated Integration Role"),
                role.permissions(),
                role.auditInfo(),
                role.tier(),
                role.isSystem()
            );
            roleRepository.update(updated);

            // Then
            Optional<Role> found = roleRepository.findById(testRoleId);
            assertThat(found).isPresent();
            assertThat(found.get().name().value()).isEqualTo("Updated Integration Role");
        }

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should check role existence")
        void shouldCheckRoleExistence() {
            // Given - role exists
            // When/Then
            assertThat(roleRepository.existsById(testRoleId)).isTrue();
            assertThat(roleRepository.existsById(RoleId.newId())).isFalse();
        }

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should find all roles")
        void shouldFindAllRoles() {
            // When
            List<Role> allRoles = roleRepository.findAllRoles();

            // Then
            assertThat(allRoles).isNotEmpty();
            assertThat(allRoles).anyMatch(r -> r.code().value().equals("TEST_ROLE_IT"));
        }

        @Test
        @Order(6)
        @Transactional
        @DisplayName("should delete role by id")
        void shouldDeleteRoleById() {
            // Given - role exists
            assertThat(roleRepository.existsById(testRoleId)).isTrue();

            // When
            roleRepository.deleteById(testRoleId);

            // Then
            assertThat(roleRepository.existsById(testRoleId)).isFalse();
        }

        @Test
        @Order(7)
        @Transactional
        @DisplayName("should return empty when role not found")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            RoleId nonExistentId = RoleId.newId();

            // When
            Optional<Role> found = roleRepository.findById(nonExistentId);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @Order(8)
        @Transactional
        @DisplayName("should return empty when code not found")
        void shouldReturnEmptyWhenCodeNotFound() {
            // Given
            RoleCode nonExistentCode = new RoleCode("NON_EXISTENT_CODE");

            // When
            Optional<Role> found = roleRepository.findByCode(nonExistentCode);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Role with Permissions")
    class RoleWithPermissions {

        @Test
        @Transactional
        @DisplayName("should save role with permissions")
        void shouldSaveRoleWithPermissions() {
            // Given
            PermissionId perm1 = randomPermissionId();
            PermissionId perm2 = randomPermissionId();

            Role role = aRole()
                .withCode("ROLE_WITH_PERMS")
                .withName("Role With Permissions")
                .withPermission(perm1)
                .withPermission(perm2)
                .build();

            // When
            roleRepository.save(role);

            // Then
            Optional<Role> found = roleRepository.findById(role.id());
            assertThat(found).isPresent();
            assertThat(found.get().permissions()).hasSize(2);
            assertThat(found.get().permissions()).contains(perm1, perm2);

            // Cleanup
            roleRepository.deleteById(role.id());
        }
    }

    @Nested
    @DisplayName("System Roles")
    class SystemRoles {

        @Test
        @Transactional
        @DisplayName("should persist system role flag")
        void shouldPersistSystemRoleFlag() {
            // Given
            Role systemRole = aRole()
                .withCode("SYSTEM_ROLE_IT")
                .asSystem()
                .build();

            // When
            roleRepository.save(systemRole);

            // Then
            Optional<Role> found = roleRepository.findById(systemRole.id());
            assertThat(found).isPresent();
            assertThat(found.get().isSystem()).isTrue();

            // Cleanup
            roleRepository.deleteById(systemRole.id());
        }

        @Test
        @Transactional
        @DisplayName("should persist non-system role flag")
        void shouldPersistNonSystemRoleFlag() {
            // Given
            Role nonSystemRole = aRole()
                .withCode("NON_SYSTEM_ROLE_IT")
                .asNonSystem()
                .build();

            // When
            roleRepository.save(nonSystemRole);

            // Then
            Optional<Role> found = roleRepository.findById(nonSystemRole.id());
            assertThat(found).isPresent();
            assertThat(found.get().isSystem()).isFalse();

            // Cleanup
            roleRepository.deleteById(nonSystemRole.id());
        }
    }

    @Nested
    @DisplayName("Tier Persistence")
    class TierPersistence {

        @Test
        @Transactional
        @DisplayName("should persist TIER_0 role")
        void shouldPersistTier0Role() {
            // Given
            Role tier0Role = aRole()
                .withCode("TIER0_IT")
                .withTier(SecurityTier.TIER_0)
                .build();

            // When
            roleRepository.save(tier0Role);

            // Then
            Optional<Role> found = roleRepository.findById(tier0Role.id());
            assertThat(found).isPresent();
            assertThat(found.get().tier()).isEqualTo(SecurityTier.TIER_0);

            // Cleanup
            roleRepository.deleteById(tier0Role.id());
        }

        @Test
        @Transactional
        @DisplayName("should persist TIER_1 role")
        void shouldPersistTier1Role() {
            // Given
            Role tier1Role = aRole()
                .withCode("TIER1_IT")
                .withTier(SecurityTier.TIER_1)
                .build();

            // When
            roleRepository.save(tier1Role);

            // Then
            Optional<Role> found = roleRepository.findById(tier1Role.id());
            assertThat(found).isPresent();
            assertThat(found.get().tier()).isEqualTo(SecurityTier.TIER_1);

            // Cleanup
            roleRepository.deleteById(tier1Role.id());
        }
    }
}
