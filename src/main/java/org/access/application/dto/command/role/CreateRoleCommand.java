package org.access.application.dto.command.role;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleCommand (
    @NotBlank String code,
    @NotBlank String name
) {}