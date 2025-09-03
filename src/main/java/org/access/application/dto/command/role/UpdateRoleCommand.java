package org.access.application.dto.command.role;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleCommand(
    @NotNull UUID roleId,
    @NotBlank String code,
    @NotBlank String name
) {}