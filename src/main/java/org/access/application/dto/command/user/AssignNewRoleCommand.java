package org.access.application.dto.command.user;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignNewRoleCommand (
    @NotNull UUID userId,
    @NotNull UUID roleId
) {}