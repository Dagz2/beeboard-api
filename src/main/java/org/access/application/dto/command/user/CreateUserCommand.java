package org.access.application.dto.command.user;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserCommand (
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String email,
    @NotNull UUID roleId,
    @NotNull @Future LocalDate contractEndDate
) {}