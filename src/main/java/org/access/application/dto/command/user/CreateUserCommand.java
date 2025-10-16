package org.access.application.dto.command.user;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserCommand (
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String email,
    @NotBlank @Size(min = 32, message = "Password must be at least 32 characters long") String password,
    @NotNull UUID roleId,
    @NotNull @Future LocalDate contractEndDate
) {}