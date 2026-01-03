package org.access.application.usecase.command.auth;

import org.access.application.dto.command.auth.CompletePasswordResetCommand;
import org.access.domain.model.*;
import org.access.domain.repository.PasswordResetRequestRepository;
import org.access.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompletePasswordReset Use Case")
class CompletePasswordResetTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordResetRequestRepository resetRepository;

    CompletePasswordReset useCase;

    // Valid 64-character hex token for tests
    static final String VALID_TOKEN = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";
    static final String NEW_PASSWORD = "NewSecurePassword123!@#$%^&*()_+abcd";

    @BeforeEach
    void setUp() {
        useCase = new CompletePasswordReset(userRepository, resetRepository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should reset password with valid token")
        void shouldResetPasswordWithValidToken() {
            // Given
            UserId userId = randomUserId();
            User user = aUser()
                .withId(userId)
                .unlocked()
                .build();

            PasswordResetRequest resetRequest = aPasswordResetRequest()
                .withUserId(userId)
                .withToken(new ResetToken(VALID_TOKEN))
                .build();

            when(resetRepository.findByToken(any(ResetToken.class)))
                .thenReturn(Optional.of(resetRequest));
            when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

            CompletePasswordResetCommand command = new CompletePasswordResetCommand(VALID_TOKEN, NEW_PASSWORD);

            // When
            useCase.execute(command);

            // Then
            verify(userRepository).update(user);
            verify(resetRepository).update(resetRequest);
        }

        @Test
        @DisplayName("should reject invalid token")
        void shouldRejectInvalidToken() {
            // Given
            when(resetRepository.findByToken(any(ResetToken.class)))
                .thenReturn(Optional.empty());

            CompletePasswordResetCommand command = new CompletePasswordResetCommand(VALID_TOKEN, NEW_PASSWORD);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired reset token");
        }

        @Test
        @DisplayName("should reject expired token")
        void shouldRejectExpiredToken() {
            // Given
            PasswordResetRequest expiredRequest = aPasswordResetRequest()
                .withToken(new ResetToken(VALID_TOKEN))
                .expired()
                .build();

            when(resetRepository.findByToken(any(ResetToken.class)))
                .thenReturn(Optional.of(expiredRequest));

            CompletePasswordResetCommand command = new CompletePasswordResetCommand(VALID_TOKEN, NEW_PASSWORD);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired reset token");
        }

        @Test
        @DisplayName("should reject already used token")
        void shouldRejectUsedToken() {
            // Given
            PasswordResetRequest usedRequest = aPasswordResetRequest()
                .withToken(new ResetToken(VALID_TOKEN))
                .used()
                .build();

            when(resetRepository.findByToken(any(ResetToken.class)))
                .thenReturn(Optional.of(usedRequest));

            CompletePasswordResetCommand command = new CompletePasswordResetCommand(VALID_TOKEN, NEW_PASSWORD);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired reset token");
        }

        @Test
        @DisplayName("should reject when user not found")
        void shouldRejectWhenUserNotFound() {
            // Given
            UserId userId = randomUserId();
            PasswordResetRequest resetRequest = aPasswordResetRequest()
                .withUserId(userId)
                .withToken(new ResetToken(VALID_TOKEN))
                .build();

            when(resetRepository.findByToken(any(ResetToken.class)))
                .thenReturn(Optional.of(resetRequest));
            when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

            CompletePasswordResetCommand command = new CompletePasswordResetCommand(VALID_TOKEN, NEW_PASSWORD);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not found for reset request");
        }

        @Test
        @DisplayName("should reject when user is locked")
        void shouldRejectWhenUserIsLocked() {
            // Given
            UserId userId = randomUserId();
            User lockedUser = aUser()
                .withId(userId)
                .locked()
                .build();

            PasswordResetRequest resetRequest = aPasswordResetRequest()
                .withUserId(userId)
                .withToken(new ResetToken(VALID_TOKEN))
                .build();

            when(resetRepository.findByToken(any(ResetToken.class)))
                .thenReturn(Optional.of(resetRequest));
            when(userRepository.findById(userId))
                .thenReturn(Optional.of(lockedUser));

            CompletePasswordResetCommand command = new CompletePasswordResetCommand(VALID_TOKEN, NEW_PASSWORD);

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot reset password for locked account");
        }

        @Test
        @DisplayName("should mark token as used after successful reset")
        void shouldMarkTokenAsUsed() {
            // Given
            UserId userId = randomUserId();
            User user = aUser()
                .withId(userId)
                .unlocked()
                .build();

            PasswordResetRequest resetRequest = aPasswordResetRequest()
                .withUserId(userId)
                .withToken(new ResetToken(VALID_TOKEN))
                .build();

            when(resetRepository.findByToken(any(ResetToken.class)))
                .thenReturn(Optional.of(resetRequest));
            when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

            CompletePasswordResetCommand command = new CompletePasswordResetCommand(VALID_TOKEN, NEW_PASSWORD);

            // When
            useCase.execute(command);

            // Then
            assertThat(resetRequest.getIsUsed()).isTrue();
            verify(resetRepository).update(resetRequest);
        }
    }
}
