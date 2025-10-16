package org.access.application.dto.command.role;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record GrantPermissionCommand (
    @NotNull(message = "Role ID is required") UUID roleId,
    @NotNull(message = "Permission ID is required") UUID permissionId
) {}