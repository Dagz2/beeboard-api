package org.access.application.usecase.command.auth;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.access.application.dto.command.auth.LoginCommand;
import org.access.application.dto.response.AuthenticationResponse;
import org.access.application.service.JwtService;
import org.access.domain.model.EmailAddress;
import org.access.domain.model.User;
import org.access.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("AuthenticateUser Use Case")
class AuthenticateUserTest {

    @Inject
    AuthenticateUser authenticateUser;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    JwtService jwtService;

    @BeforeEach
    void setUp() {
        reset(userRepository, jwtService);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should authenticate user with valid credentials")
        void shouldAuthenticateWithValidCredentials() {
            // Given
            User user = aUser()
                .withEmail("alice@example.com")
                .unlocked()
                .build();

            when(userRepository.findByMail(any(EmailAddress.class)))
                .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(User.class)))
                .thenReturn("jwt-token-123");

            LoginCommand command = new LoginCommand("alice@example.com", VALID_PASSWORD);

            // When
            AuthenticationResponse response = authenticateUser.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token-123");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.expiresIn()).isEqualTo(3600L);
            verify(userRepository).update(user);
        }

        @Test
        @DisplayName("should reject authentication when user not found")
        void shouldRejectWhenUserNotFound() {
            // Given
            when(userRepository.findByMail(any(EmailAddress.class)))
                .thenReturn(Optional.empty());

            LoginCommand command = new LoginCommand("unknown@example.com", "password");

            // When/Then
            assertThatThrownBy(() -> authenticateUser.execute(command))
                .isInstanceOf(NotAuthorizedException.class);
        }

        @Test
        @DisplayName("should reject authentication when account is locked")
        void shouldRejectWhenAccountIsLocked() {
            // Given
            User lockedUser = aUser()
                .withEmail("locked@example.com")
                .locked()
                .build();

            when(userRepository.findByMail(any(EmailAddress.class)))
                .thenReturn(Optional.of(lockedUser));

            LoginCommand command = new LoginCommand("locked@example.com", VALID_PASSWORD);

            // When/Then
            assertThatThrownBy(() -> authenticateUser.execute(command))
                .isInstanceOf(NotAuthorizedException.class);
        }

        @Test
        @DisplayName("should reject authentication with invalid password")
        void shouldRejectWithInvalidPassword() {
            // Given
            User user = aUser()
                .withEmail("alice@example.com")
                .unlocked()
                .build();

            when(userRepository.findByMail(any(EmailAddress.class)))
                .thenReturn(Optional.of(user));

            LoginCommand command = new LoginCommand("alice@example.com", "wrongpassword123!@#$%^&*()abcd");

            // When/Then
            assertThatThrownBy(() -> authenticateUser.execute(command))
                .isInstanceOf(NotAuthorizedException.class);
        }

        @Test
        @DisplayName("should reject authentication when contract is expired")
        void shouldRejectWhenContractExpired() {
            // Given
            User expiredUser = aUser()
                .withEmail("expired@example.com")
                .withExpiredContract()
                .unlocked()
                .build();

            when(userRepository.findByMail(any(EmailAddress.class)))
                .thenReturn(Optional.of(expiredUser));

            LoginCommand command = new LoginCommand("expired@example.com", VALID_PASSWORD);

            // When/Then
            assertThatThrownBy(() -> authenticateUser.execute(command))
                .isInstanceOf(NotAuthorizedException.class);
        }

        @Test
        @DisplayName("should record activity on successful authentication")
        void shouldRecordActivityOnSuccess() {
            // Given
            User user = aUser()
                .withEmail("alice@example.com")
                .unlocked()
                .build();

            when(userRepository.findByMail(any(EmailAddress.class)))
                .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(User.class)))
                .thenReturn("jwt-token");

            LoginCommand command = new LoginCommand("alice@example.com", VALID_PASSWORD);

            // When
            authenticateUser.execute(command);

            // Then
            verify(userRepository).update(user);
        }

        @Test
        @DisplayName("should not update user when authentication fails")
        void shouldNotUpdateUserWhenAuthFails() {
            // Given
            when(userRepository.findByMail(any(EmailAddress.class)))
                .thenReturn(Optional.empty());

            LoginCommand command = new LoginCommand("unknown@example.com", "password");

            // When/Then
            assertThatThrownBy(() -> authenticateUser.execute(command))
                .isInstanceOf(NotAuthorizedException.class);

            verify(userRepository, never()).update(any());
        }
    }
}
