package org.access.application.mapper;

import org.access.application.dto.query.PermissionDto;
import org.access.domain.model.Permission;
import org.access.domain.model.SecurityTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("PermissionMapper")
class PermissionMapperTest {

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("should throw IllegalArgumentException when permission is null")
        void shouldThrowWhenPermissionIsNull() {
            assertThatThrownBy(() -> PermissionMapper.toDto(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission cannot be null");
        }

        @Test
        @DisplayName("should map all permission fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // Given
            var permId = randomPermissionId();
            Permission permission = aPermission()
                .withId(permId)
                .withCode("USER_CREATE")
                .withName("Create User")
                .withDescription("Allows creating new users")
                .withTier(SecurityTier.TIER_1)
                .build();

            // When
            PermissionDto dto = PermissionMapper.toDto(permission);

            // Then
            assertThat(dto.getPermissionId()).isEqualTo(permId.id());
            assertThat(dto.getCode()).isEqualTo("USER_CREATE");
            assertThat(dto.getName()).isEqualTo("Create User");
            assertThat(dto.getDescription()).isEqualTo("Allows creating new users");
        }

        @Test
        @DisplayName("should map permission with empty description")
        void shouldMapPermissionWithEmptyDescription() {
            // Given
            Permission permission = aPermission()
                .withDescription("")
                .build();

            // When
            PermissionDto dto = PermissionMapper.toDto(permission);

            // Then
            assertThat(dto.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("should map system permission correctly")
        void shouldMapSystemPermission() {
            // Given
            Permission permission = aPermission()
                .withCode("SYSTEM_ADMIN")
                .withName("System Administration")
                .asSystem()
                .build();

            // When
            PermissionDto dto = PermissionMapper.toDto(permission);

            // Then
            assertThat(dto.getCode()).isEqualTo("SYSTEM_ADMIN");
            assertThat(dto.getName()).isEqualTo("System Administration");
        }

        @Test
        @DisplayName("should map non-system permission correctly")
        void shouldMapNonSystemPermission() {
            // Given
            Permission permission = aPermission()
                .withCode("VIEW_REPORTS")
                .asNonSystem()
                .build();

            // When
            PermissionDto dto = PermissionMapper.toDto(permission);

            // Then
            assertThat(dto.getCode()).isEqualTo("VIEW_REPORTS");
        }

        @Test
        @DisplayName("should preserve special characters in description")
        void shouldPreserveSpecialCharactersInDescription() {
            // Given
            String description = "Permission for <admin> actions & \"special\" operations";
            Permission permission = aPermission()
                .withDescription(description)
                .build();

            // When
            PermissionDto dto = PermissionMapper.toDto(permission);

            // Then
            assertThat(dto.getDescription()).isEqualTo(description);
        }
    }
}
