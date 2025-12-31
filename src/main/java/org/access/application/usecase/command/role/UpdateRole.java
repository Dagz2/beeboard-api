package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.role.UpdateRoleCommand;
import org.access.application.service.AuditService;
import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleId;
import org.access.domain.model.RoleName;
import org.access.domain.model.SecurityContext;
import org.access.domain.repository.RoleRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.access.application.service.SecurityContextService;

/**
 * Use case for updating a role's code and name.
 *
 * This command updates an existing role's code and name fields
 * and persists the changes using the {@link RoleRepository}.
 *
 * Security validations:
 * <ul>
 *   <li>Only TIER_0 actors can modify system roles</li>
 * </ul>
 */
@ApplicationScoped
public class UpdateRole {

    private final RoleRepository roleRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;
    private final SecurityContextService securityContextService;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param roleRepository         the repository for accessing and saving roles
     * @param privilegeGuard         the guard for privilege escalation protection
     * @param auditService           the service for audit logging
     * @param securityContextService the service for security context invalidation
     */
    @Inject
    public UpdateRole(RoleRepository roleRepository, PrivilegeEscalationGuard privilegeGuard,
                      AuditService auditService, SecurityContextService securityContextService) {
        this.roleRepository = roleRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
        this.securityContextService = securityContextService;
    }

    /**
     * Executes the command to update a role's code and name.
     *
     * @param command         the command containing the role ID and new values
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the role does not exist
     * @throws org.access.domain.exception.SystemResourceProtectedException if non-TIER_0 actor tries to modify system role
     */
    @Transactional
    public void execute(UpdateRoleCommand command, SecurityContext securityContext) {
        Role role = roleRepository.findById(new RoleId(command.roleId()))
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + command.roleId()));

        // Validate system role modification (only TIER_0 can modify system roles)
        privilegeGuard.validateSystemRoleModification(securityContext.actorTier(), role);

        String oldCode = role.code().value();
        String oldName = role.name().value();

        role.updateCode(new RoleCode(command.code()));
        role.updateName(new RoleName(command.name()));

        roleRepository.update(role);

        // Audit the operation
        auditService.record(
            securityContext,
            "UPDATE_ROLE",
            "ROLE",
            command.roleId().toString(),
            "{\"code\": \"" + oldCode + "\", \"name\": \"" + oldName + "\"}",
            "{\"code\": \"" + command.code() + "\", \"name\": \"" + command.name() + "\"}"
        );

        // Invalidate security context for all users with this role
        securityContextService.invalidateByRole(new RoleId(command.roleId()));
    }
}
