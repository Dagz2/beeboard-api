package org.access.application.usecase.command.auth;

import jakarta.ws.rs.ClientErrorException;
import org.access.application.dto.command.auth.RequestPasswordResetCommand;
import org.access.application.service.EmailService;
import org.access.application.service.RateLimitService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestPasswordReset Use Case")
class RequestPasswordResetTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordResetRequestRepository resetRepository;

    @Mock
    EmailService emailService;

    @Mock
    RateLimitService rateLimitService;

    RequestPasswordReset useCase;

    @BeforeEach
    void setUp() {
        useCase = new RequestPasswordReset(
            userRepository,
            resetRepository,
            emailService,
            rateLimitService
        );
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should send password reset email for valid user")
        void shouldSendResetEmailForValidUser() {
            // Given
            User user = aUser()
                .withEmail("alice@example.com")
                .withFullName("Alice", "Martin")
                .unlocked()
                .build();

            when(rateLimitService.isRateLimitExceeded("alice@example.com")).thenReturn(false);
            when(userRepository.findByMail(any(EmailAddress.class))).thenReturn(Optional.of(user));

            RequestPasswordResetCommand command = new RequestPasswordResetCommand("alice@example.com");

            // When
            useCase.execute(command);

            // Then
            verify(resetRepository).invalidateAllForUser(user.getId());
            verify(resetRepository).save(any(PasswordResetRequest.class));
            verify(emailService).sendPasswordResetEmail(
                eq(user.getEmail()),
                any(ResetToken.class),
                eq(user.getFullName())
            );
            verify(rateLimitService).incrementRateLimit("alice@example.com");
        }

        @Test
        @DisplayName("should throw 429 when rate limit exceeded")
        void shouldThrowWhenRateLimitExceeded() {
            // Given
            when(rateLimitService.isRateLimitExceeded("spam@example.com")).thenReturn(true);

            RequestPasswordResetCommand command = new RequestPasswordResetCommand("spam@example.com");

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClientErrorException.class)
                .hasMessageContaining("Too many password reset requests");

            verify(userRepository, never()).findByMail(any());
            verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
        }

        @Test
        @DisplayName("should silently succeed when user not found (prevents enumeration)")
        void shouldSilentlySucceedWhenUserNotFound() {
            // Given
            when(rateLimitService.isRateLimitExceeded("unknown@example.com")).thenReturn(false);
            when(userRepository.findByMail(any(EmailAddress.class))).thenReturn(Optional.empty());

            RequestPasswordResetCommand command = new RequestPasswordResetCommand("unknown@example.com");

            // When/Then
            assertThatCode(() -> useCase.execute(command)).doesNotThrowAnyException();

            verify(rateLimitService).incrementRateLimit("unknown@example.com");
            verify(resetRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
        }

        @Test
        @DisplayName("should silently succeed when account is locked (prevents status enumeration)")
        void shouldSilentlySucceedWhenAccountLocked() {
            // Given
            User lockedUser = aUser()
                .withEmail("locked@example.com")
                .locked()
                .build();

            when(rateLimitService.isRateLimitExceeded("locked@example.com")).thenReturn(false);
            when(userRepository.findByMail(any(EmailAddress.class))).thenReturn(Optional.of(lockedUser));

            RequestPasswordResetCommand command = new RequestPasswordResetCommand("locked@example.com");

            // When/Then
            assertThatCode(() -> useCase.execute(command)).doesNotThrowAnyException();

            verify(rateLimitService).incrementRateLimit("locked@example.com");
            verify(resetRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
        }

        @Test
        @DisplayName("should invalidate existing tokens before creating new one")
        void shouldInvalidateExistingTokens() {
            // Given
            User user = aUser()
                .withEmail("alice@example.com")
                .unlocked()
                .build();

            when(rateLimitService.isRateLimitExceeded(any())).thenReturn(false);
            when(userRepository.findByMail(any(EmailAddress.class))).thenReturn(Optional.of(user));

            RequestPasswordResetCommand command = new RequestPasswordResetCommand("alice@example.com");

            // When
            useCase.execute(command);

            // Then
            verify(resetRepository).invalidateAllForUser(user.getId());
            verify(resetRepository).save(any(PasswordResetRequest.class));
        }

        @Test
        @DisplayName("should throw exception when email service fails")
        void shouldThrowWhenEmailServiceFails() {
            // Given
            User user = aUser()
                .withEmail("alice@example.com")
                .unlocked()
                .build();

            when(rateLimitService.isRateLimitExceeded(any())).thenReturn(false);
            when(userRepository.findByMail(any(EmailAddress.class))).thenReturn(Optional.of(user));
            doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendPasswordResetEmail(any(), any(), any());

            RequestPasswordResetCommand command = new RequestPasswordResetCommand("alice@example.com");

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to send reset email");
        }
    }
}
