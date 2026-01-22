package org.access.application.dto.command.role;

import jakarta.validation.constraints.NotNull;
import org.shared.domain.ApplicationRole;

import java.util.UUID;

/**
 * Command to update a role's ApplicationRole.
 *
 * <p>This command allows SUPER_ADMIN users to change the ApplicationRole
 * of an existing role. ApplicationRole is used for cross-BC communication
 * and coarse-grained authorization.</p>
 *
 * <p>Security: Only TIER_0 (SUPER_ADMIN) users can execute this command.</p>
 */
public record UpdateRoleApplicationRoleCommand(
    @NotNull(message = "Role ID is required") UUID roleId,
    @NotNull(message = "ApplicationRole is required") ApplicationRole newApplicationRole
) {}
