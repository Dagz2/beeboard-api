package org.access.application.dto.command.user;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record RecordActivityCommand (
    @NotNull UUID userId
) {}