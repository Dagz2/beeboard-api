package org.access.application.mapper;

import org.access.application.dto.query.UserDto;
import org.access.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("UserMapper")
class UserMapperTest {

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("should throw IllegalArgumentException when user is null")
        void shouldThrowWhenUserIsNull() {
            assertThatThrownBy(() -> UserMapper.toDto(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null");
        }

        @Test
        @DisplayName("should map all user fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // Given
            var roleId = randomRoleId();
            var userId = randomUserId();
            var contractDate = LocalDate.of(2025, 12, 31);

            User user = aUser()
                .withId(userId)
                .withFullName("Alice", "Martin")
                .withEmail("alice.martin@example.com")
                .withRoleId(roleId)
                .withContractEndDate(contractDate)
                .locked()
                .build();

            // When
            UserDto dto = UserMapper.toDto(user);

            // Then
            assertThat(dto.getUserId()).isEqualTo(userId.id());
            assertThat(dto.getFirstName()).isEqualTo("Alice");
            assertThat(dto.getLastName()).isEqualTo("Martin");
            assertThat(dto.getEmail()).isEqualTo("alice.martin@example.com");
            assertThat(dto.getContractEndDate()).isEqualTo(contractDate);
            assertThat(dto.getIsLocked()).isTrue();
            assertThat(dto.getRoleId()).isEqualTo(roleId.id());
        }

        @Test
        @DisplayName("should map unlocked user correctly")
        void shouldMapUnlockedUserCorrectly() {
            // Given
            User user = aUser()
                .unlocked()
                .build();

            // When
            UserDto dto = UserMapper.toDto(user);

            // Then
            assertThat(dto.getIsLocked()).isFalse();
        }

        @Test
        @DisplayName("should map audit info correctly")
        void shouldMapAuditInfoCorrectly() {
            // Given
            User user = aUser().build();

            // When
            UserDto dto = UserMapper.toDto(user);

            // Then
            assertThat(dto.getAuditInfo()).isNotNull();
            assertThat(dto.getAuditInfo().getCreatedAt()).isNotNull();
            assertThat(dto.getAuditInfo().getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should map last activity date")
        void shouldMapLastActivityDate() {
            // Given
            User user = aUser().build();

            // When
            UserDto dto = UserMapper.toDto(user);

            // Then
            assertThat(dto.getLastActivityDate()).isNotNull();
            assertThat(dto.getLastActivityDate()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("should preserve contract end date for expired contracts")
        void shouldPreserveExpiredContractDate() {
            // Given
            var expiredDate = LocalDate.now().minusDays(10);
            User user = aUser()
                .withContractEndDate(expiredDate)
                .build();

            // When
            UserDto dto = UserMapper.toDto(user);

            // Then
            assertThat(dto.getContractEndDate()).isEqualTo(expiredDate);
        }
    }
}
