package org.access.application.dto.command.user;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeFullNameCommand (
    @NotNull(message = "User ID is required") UUID userId,
    @NotBlank(message = "First Name is required") String firstName,
    @NotBlank(message = "Last Name is required") String lastName
) {}