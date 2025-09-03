package org.access.application.dto.command.permission;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePermissionCommand(
    @NotNull UUID permissionId,
    @NotBlank String code,
    @NotBlank String name,
    @NotBlank String description
) {}