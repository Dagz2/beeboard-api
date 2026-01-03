package org.access.application.mapper;

import org.access.application.dto.query.RoleDto;
import org.access.domain.model.PermissionId;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("RoleMapper")
class RoleMapperTest {

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("should throw IllegalArgumentException when role is null")
        void shouldThrowWhenRoleIsNull() {
            assertThatThrownBy(() -> RoleMapper.toDto(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role cannot be null");
        }

        @Test
        @DisplayName("should map all role fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // Given
            var roleId = randomRoleId();
            Role role = aRole()
                .withId(roleId)
                .withCode("ADMIN")
                .withName("Administrator")
                .withTier(SecurityTier.TIER_1)
                .build();

            // When
            RoleDto dto = RoleMapper.toDto(role);

            // Then
            assertThat(dto.getRoleId()).isEqualTo(roleId.id());
            assertThat(dto.getCode()).isEqualTo("ADMIN");
            assertThat(dto.getName()).isEqualTo("Administrator");
        }

        @Test
        @DisplayName("should map role with empty permissions")
        void shouldMapRoleWithEmptyPermissions() {
            // Given
            Role role = aRole().build();

            // When
            RoleDto dto = RoleMapper.toDto(role);

            // Then
            assertThat(dto.getPermissions()).isNotNull();
            assertThat(dto.getPermissions()).isEmpty();
        }

        @Test
        @DisplayName("should map role with multiple permissions")
        void shouldMapRoleWithMultiplePermissions() {
            // Given
            PermissionId perm1 = randomPermissionId();
            PermissionId perm2 = randomPermissionId();
            PermissionId perm3 = randomPermissionId();

            Role role = aRole()
                .withPermission(perm1)
                .withPermission(perm2)
                .withPermission(perm3)
                .build();

            // When
            RoleDto dto = RoleMapper.toDto(role);

            // Then
            assertThat(dto.getPermissions()).hasSize(3);
            assertThat(dto.getPermissions()).containsExactlyInAnyOrder(
                perm1.id(),
                perm2.id(),
                perm3.id()
            );
        }

        @Test
        @DisplayName("should map audit info correctly")
        void shouldMapAuditInfoCorrectly() {
            // Given
            Role role = aRole().build();

            // When
            RoleDto dto = RoleMapper.toDto(role);

            // Then
            assertThat(dto.getAuditInfo()).isNotNull();
            assertThat(dto.getAuditInfo().getCreatedAt()).isNotNull();
            assertThat(dto.getAuditInfo().getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should map SUPER_ADMIN role correctly")
        void shouldMapSuperAdminRole() {
            // Given
            Role role = aRole()
                .asSuperAdmin()
                .build();

            // When
            RoleDto dto = RoleMapper.toDto(role);

            // Then
            assertThat(dto.getCode()).isEqualTo("SUPER_ADMIN");
            assertThat(dto.getName()).isEqualTo("Super Administrator");
        }

        @Test
        @DisplayName("should map permissions as UUID set")
        void shouldMapPermissionsAsUuidSet() {
            // Given
            PermissionId permId = randomPermissionId();
            Role role = aRole()
                .withPermission(permId)
                .build();

            // When
            RoleDto dto = RoleMapper.toDto(role);

            // Then
            Set<UUID> permissions = dto.getPermissions();
            assertThat(permissions).allSatisfy(uuid ->
                assertThat(uuid).isInstanceOf(UUID.class)
            );
        }
    }
}
