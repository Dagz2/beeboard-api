package org.access.application.dto.command.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.access.domain.model.SecurityTier;
import org.shared.domain.ApplicationRole;

public record CreateRoleCommand (
    @NotBlank(message = "Role code is required") String code,
    @NotBlank(message = "Role name is required") String name,
    @NotNull(message = "Security tier is required") SecurityTier tier,
    @NotNull(message = "System flag is required") Boolean isSystem,
    @NotNull(message = "Application role is required") ApplicationRole applicationRole
) {}