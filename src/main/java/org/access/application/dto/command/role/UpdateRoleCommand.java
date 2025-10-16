package org.access.application.dto.command.role;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleCommand(
    @NotNull(message = "Role ID is required") UUID roleId,
    @NotBlank(message = "Role code is required") String code,
    @NotBlank(message = "Role name is required") String name
) {}