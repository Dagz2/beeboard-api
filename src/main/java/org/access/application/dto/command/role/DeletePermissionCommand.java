package org.access.application.dto.command.role;

import java.util.UUID;

import io.smallrye.common.constraint.NotNull;

public record DeletePermissionCommand (
    @NotNull UUID roleId
) {}