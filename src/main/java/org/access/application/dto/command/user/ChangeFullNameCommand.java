package org.access.application.dto.command.user;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeFullNameCommand (
    @NotNull UUID userId,
    @NotBlank String firstName,
    @NotBlank String lastName
) {}