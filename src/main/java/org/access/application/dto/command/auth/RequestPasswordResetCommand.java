package org.access.application.dto.command.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Command to request a password reset for a user.
 *
 * This initiates the password reset flow by sending an email with a
 * time-limited reset token to the specified email address.
 */
public record RequestPasswordResetCommand(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email
) {}
