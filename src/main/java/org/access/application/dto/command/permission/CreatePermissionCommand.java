package org.access.application.dto.command.permission;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionCommand (
    @NotBlank String code,
    @NotBlank String name,
    @NotBlank String description
) {}