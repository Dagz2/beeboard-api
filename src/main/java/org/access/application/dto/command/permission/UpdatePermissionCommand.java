package org.access.application.dto.command.permission;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePermissionCommand(
    @NotNull(message = "Permission ID is required") UUID permissionId,
    @NotBlank(message = "Permission code is required") String code,
    @NotBlank(message = "Permission name is required") String name,
    @NotBlank(message = "Permission description is required") String description
) {}