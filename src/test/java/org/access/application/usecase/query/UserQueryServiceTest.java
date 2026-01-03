package org.access.application.usecase.query;

import org.access.application.dto.query.UserDto;
import org.access.domain.model.*;
import org.access.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserQueryService")
class UserQueryServiceTest {

    @Mock
    UserRepository userRepository;

    UserQueryService service;

    @BeforeEach
    void setUp() {
        service = new UserQueryService(userRepository);
    }

    @Nested
    @DisplayName("findAllUsers")
    class FindAllUsers {

        @Test
        @DisplayName("should return all users as DTOs")
        void shouldReturnAllUsersAsDtos() {
            // Given
            User user1 = aUser().withEmail("user1@example.com").build();
            User user2 = aUser().withEmail("user2@example.com").build();
            when(userRepository.findAllUsers()).thenReturn(List.of(user1, user2));

            // When
            List<UserDto> result = service.findAllUsers();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
        }

        @Test
        @DisplayName("should return empty list when no users")
        void shouldReturnEmptyListWhenNoUsers() {
            // Given
            when(userRepository.findAllUsers()).thenReturn(List.of());

            // When
            List<UserDto> result = service.findAllUsers();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            // Given
            UserId userId = randomUserId();
            User user = aUser()
                .withId(userId)
                .withEmail("found@example.com")
                .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // When
            Optional<UserDto> result = service.findById(userId.id());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("found@example.com");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            UserId userId = randomUserId();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When
            Optional<UserDto> result = service.findById(userId.id());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should return user when email found")
        void shouldReturnUserWhenEmailFound() {
            // Given
            EmailAddress email = new EmailAddress("test@example.com");
            User user = aUser()
                .withEmail(email)
                .build();
            when(userRepository.findByMail(email)).thenReturn(Optional.of(user));

            // When
            Optional<UserDto> result = service.findByEmail(email);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // Given
            EmailAddress email = new EmailAddress("unknown@example.com");
            when(userRepository.findByMail(email)).thenReturn(Optional.empty());

            // When
            Optional<UserDto> result = service.findByEmail(email);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("isContractExpired")
    class IsContractExpired {

        @Test
        @DisplayName("should return true when contract expired")
        void shouldReturnTrueWhenContractExpired() {
            // Given
            UserId userId = randomUserId();
            User expiredUser = aUser()
                .withId(userId)
                .withExpiredContract()
                .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(expiredUser));

            // When
            boolean result = service.isContractExpired(userId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when contract active")
        void shouldReturnFalseWhenContractActive() {
            // Given
            UserId userId = randomUserId();
            User activeUser = aUser()
                .withId(userId)
                .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));

            // When
            boolean result = service.isContractExpired(userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            // Given
            UserId userId = randomUserId();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.isContractExpired(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("findUsersByRole")
    class FindUsersByRole {

        @Test
        @DisplayName("should return users with specified role")
        void shouldReturnUsersWithRole() {
            // Given
            RoleId roleId = randomRoleId();
            User user1 = aUser().withRoleId(roleId).withEmail("user1@example.com").build();
            User user2 = aUser().withRoleId(roleId).withEmail("user2@example.com").build();
            when(userRepository.findByRoleId(roleId)).thenReturn(List.of(user1, user2));

            // When
            List<UserDto> result = service.findUsersByRole(roleId);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when no users with role")
        void shouldReturnEmptyListWhenNoUsersWithRole() {
            // Given
            RoleId roleId = randomRoleId();
            when(userRepository.findByRoleId(roleId)).thenReturn(List.of());

            // When
            List<UserDto> result = service.findUsersByRole(roleId);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
