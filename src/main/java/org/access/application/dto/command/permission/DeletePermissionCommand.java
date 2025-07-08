package org.access.application.dto.command.permission;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record DeletePermissionCommand (
    @NotNull UUID permissionId
) {}