package org.access.application.dto.command.user;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserCommand (
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    @NotBlank(message = "Email is required") String email,
    @NotBlank(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min = 32, message = "Password must be at least 32 characters long")
    @Pattern(
        regexp = ".*\\d.*", 
        message = "Password must contain at least one digit (0-9)"
    )
    @Pattern(
        regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{}';:\",.<>/?|\\\\].*",
        message = "Password must contain at least one special character"
    )
    String password,
    
    @NotNull(message = "Role ID is required") UUID roleId,
    @NotNull(message = "Contract end date is required") @Future(message = "Contract end date must be in the future") LocalDate contractEndDate
) {}