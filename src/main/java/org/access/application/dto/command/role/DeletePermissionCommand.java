package org.access.application.dto.command.role;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record DeletePermissionCommand (
    @NotNull(message = "Role ID is required") UUID roleId
) {}