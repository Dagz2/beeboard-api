package org.access.application.dto.command.user;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignNewRoleCommand (
    @NotNull(message = "User ID is required") UUID userId,
    @NotNull(message = "Role ID is required") UUID roleId
) {}