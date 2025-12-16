package org.access.application.dto.command.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Command to complete a password reset using a reset token.
 *
 * The token must be valid (not expired, not used) and the new password
 * must meet the security requirements.
 *
 * Password must meet security requirements:
 * - Minimum 32 characters
 * - At least one digit (0-9)
 * - At least one special character
 */
public record CompletePasswordResetCommand(
    @NotBlank(message = "Token is required")
    @Size(min = 64, max = 64, message = "Token must be exactly 64 characters long")
    @Pattern(
        regexp = "^[a-f0-9]{64}$",
        message = "Token must contain only lowercase hexadecimal characters (a-f, 0-9)"
    )
    String token,

    @NotBlank(message = "New password is required")
    @Size(min = 32, message = "Password must be at least 32 characters long")
    @Pattern(
        regexp = ".*\\d.*",
        message = "Password must contain at least one digit (0-9)"
    )
    @Pattern(
        regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{}';:\",.<>/?|\\\\].*",
        message = "Password must contain at least one special character"
    )
    String newPassword
) {}
