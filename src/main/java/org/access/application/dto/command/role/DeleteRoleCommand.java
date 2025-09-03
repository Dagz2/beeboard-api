package org.access.application.dto.command.role;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record DeleteRoleCommand(
    @NotNull UUID roleId
) {}