package org.access.application.dto.command.user;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record DeleteUserCommand(
    @NotNull(message = "User ID is required") UUID userId
) {}