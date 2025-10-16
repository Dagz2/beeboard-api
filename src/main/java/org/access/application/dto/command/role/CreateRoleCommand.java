package org.access.application.dto.command.role;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleCommand (
    @NotBlank(message = "Role code is required") String code,
    @NotBlank(message = "Role name is required") String name
) {}