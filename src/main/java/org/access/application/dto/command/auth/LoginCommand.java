package org.access.application.dto.command.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Command DTO for user authentication.
 *
 * @param email the user's email address
 * @param password the user's plain text password
 */
public record LoginCommand(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,
    @NotBlank(message = "Password is required")
    String password
) {}
