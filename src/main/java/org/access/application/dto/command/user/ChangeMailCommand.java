package org.access.application.dto.command.user;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeMailCommand (
    @NotNull UUID userId,
    @NotBlank String email
) {}