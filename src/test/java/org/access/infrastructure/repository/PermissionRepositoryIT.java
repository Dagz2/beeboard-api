package org.access.infrastructure.repository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.domain.model.*;
import org.access.domain.repository.PermissionRepository;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@QuarkusTest
@DisplayName("PermissionRepository Integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PermissionRepositoryIT {

    @Inject
    PermissionRepository permissionRepository;

    static PermissionId testPermissionId;

    @Nested
    @DisplayName("CRUD Operations")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CrudOperations {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should save and retrieve permission")
        void shouldSaveAndRetrievePermission() {
            // Given
            Permission permission = aPermission()
                .withCode("TEST_PERM_IT")
                .withName("Integration Test Permission")
                .withDescription("Permission for integration tests")
                .withTier(SecurityTier.TIER_2)
                .build();
            testPermissionId = permission.id();

            // When
            permissionRepository.save(permission);
            Optional<Permission> found = permissionRepository.findById(testPermissionId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().code().value()).isEqualTo("TEST_PERM_IT");
            assertThat(found.get().name().value()).isEqualTo("Integration Test Permission");
            assertThat(found.get().description()).isEqualTo("Permission for integration tests");
            assertThat(found.get().tier()).isEqualTo(SecurityTier.TIER_2);
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should find permission by code")
        void shouldFindPermissionByCode() {
            // Given
            PermissionCode code = new PermissionCode("TEST_PERM_IT");

            // When
            Optional<Permission> found = permissionRepository.findByCode(code);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().id()).isEqualTo(testPermissionId);
        }

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should update existing permission")
        void shouldUpdatePermission() {
            // Given
            Optional<Permission> permOpt = permissionRepository.findById(testPermissionId);
            assertThat(permOpt).isPresent();
            Permission perm = permOpt.get();

            // When - update with new description
            Permission updated = new Permission(
                perm.id(),
                perm.code(),
                perm.name(),
                "Updated description for IT",
                perm.tier(),
                perm.isSystem()
            );
            permissionRepository.save(updated);

            // Then
            Optional<Permission> found = permissionRepository.findById(testPermissionId);
            assertThat(found).isPresent();
            assertThat(found.get().description()).isEqualTo("Updated description for IT");
        }

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should check permission existence")
        void shouldCheckPermissionExistence() {
            // When/Then
            assertThat(permissionRepository.existsById(testPermissionId)).isTrue();
            assertThat(permissionRepository.existsById(PermissionId.newId())).isFalse();
        }

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should find all permissions")
        void shouldFindAllPermissions() {
            // When
            List<Permission> allPermissions = permissionRepository.findAllPermissions();

            // Then
            assertThat(allPermissions).isNotEmpty();
            assertThat(allPermissions).anyMatch(p -> p.code().value().equals("TEST_PERM_IT"));
        }

        @Test
        @Order(6)
        @Transactional
        @DisplayName("should delete permission by id")
        void shouldDeletePermissionById() {
            // Given
            assertThat(permissionRepository.existsById(testPermissionId)).isTrue();

            // When
            permissionRepository.deleteById(testPermissionId);

            // Then
            assertThat(permissionRepository.existsById(testPermissionId)).isFalse();
        }

        @Test
        @Order(7)
        @Transactional
        @DisplayName("should return empty when permission not found by id")
        void shouldReturnEmptyWhenNotFoundById() {
            // Given
            PermissionId nonExistentId = PermissionId.newId();

            // When
            Optional<Permission> found = permissionRepository.findById(nonExistentId);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @Order(8)
        @Transactional
        @DisplayName("should return empty when permission not found by code")
        void shouldReturnEmptyWhenNotFoundByCode() {
            // Given
            PermissionCode nonExistentCode = new PermissionCode("NON_EXISTENT_PERM");

            // When
            Optional<Permission> found = permissionRepository.findByCode(nonExistentCode);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tier Persistence")
    class TierPersistence {

        @Test
        @Transactional
        @DisplayName("should persist TIER_0 permission")
        void shouldPersistTier0Permission() {
            // Given
            Permission permission = aPermission()
                .withCode("TIER0_PERM_IT")
                .withTier(SecurityTier.TIER_0)
                .build();

            // When
            permissionRepository.save(permission);

            // Then
            Optional<Permission> found = permissionRepository.findById(permission.id());
            assertThat(found).isPresent();
            assertThat(found.get().tier()).isEqualTo(SecurityTier.TIER_0);

            // Cleanup
            permissionRepository.deleteById(permission.id());
        }

        @Test
        @Transactional
        @DisplayName("should persist TIER_1 permission")
        void shouldPersistTier1Permission() {
            // Given
            Permission permission = aPermission()
                .withCode("TIER1_PERM_IT")
                .withTier(SecurityTier.TIER_1)
                .build();

            // When
            permissionRepository.save(permission);

            // Then
            Optional<Permission> found = permissionRepository.findById(permission.id());
            assertThat(found).isPresent();
            assertThat(found.get().tier()).isEqualTo(SecurityTier.TIER_1);

            // Cleanup
            permissionRepository.deleteById(permission.id());
        }
    }

    @Nested
    @DisplayName("System Permission")
    class SystemPermission {

        @Test
        @Transactional
        @DisplayName("should persist system permission flag")
        void shouldPersistSystemPermissionFlag() {
            // Given
            Permission systemPerm = aPermission()
                .withCode("SYSTEM_PERM_IT")
                .asSystem()
                .build();

            // When
            permissionRepository.save(systemPerm);

            // Then
            Optional<Permission> found = permissionRepository.findById(systemPerm.id());
            assertThat(found).isPresent();
            assertThat(found.get().isSystem()).isTrue();

            // Cleanup
            permissionRepository.deleteById(systemPerm.id());
        }

        @Test
        @Transactional
        @DisplayName("should persist non-system permission flag")
        void shouldPersistNonSystemPermissionFlag() {
            // Given
            Permission nonSystemPerm = aPermission()
                .withCode("NON_SYSTEM_PERM_IT")
                .asNonSystem()
                .build();

            // When
            permissionRepository.save(nonSystemPerm);

            // Then
            Optional<Permission> found = permissionRepository.findById(nonSystemPerm.id());
            assertThat(found).isPresent();
            assertThat(found.get().isSystem()).isFalse();

            // Cleanup
            permissionRepository.deleteById(nonSystemPerm.id());
        }
    }
}
