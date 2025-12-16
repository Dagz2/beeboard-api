package org.access.application.dto.command.user;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Command to change a user's password.
 *
 * Password must meet security requirements:
 * - Minimum 32 characters
 * - At least one digit (0-9)
 * - At least one special character
 */
public record ChangePasswordCommand(
    @NotNull(message = "User ID is required")
    UUID userId,

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
