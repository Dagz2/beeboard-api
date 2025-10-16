package org.access.application.dto.command.permission;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionCommand (
    @NotBlank(message = "Permission code is required") String code,
    @NotBlank(message = "Permission name is required") String name,
    @NotBlank(message = "Permission description is required") String description
) {}