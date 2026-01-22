package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.role.UpdateRoleApplicationRoleCommand;
import org.access.application.service.AuditService;
import org.access.application.service.SecurityContextService;
import org.access.domain.event.RoleApplicationRoleChangedEvent;
import org.access.domain.model.Role;
import org.access.domain.model.RoleId;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.SecurityTier;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.shared.domain.ApplicationRole;
import org.shared.event.EventPublisher;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case for updating a role's ApplicationRole.
 *
 * <p>This command updates an existing role's ApplicationRole field,
 * which is used for cross-BC communication and coarse-grained authorization.</p>
 *
 * <p>Security validations:</p>
 * <ul>
 *   <li>Only TIER_0 (SUPER_ADMIN) actors can update ApplicationRole</li>
 *   <li>ApplicationRole affects cross-BC authorization (high impact)</li>
 *   <li>Full audit trail is maintained for all changes</li>
 * </ul>
 *
 * <p>Rationale: ApplicationRole is a Published Language element that affects
 * how roles are interpreted across bounded contexts. Only the highest privilege
 * level should be able to modify it.</p>
 */
@ApplicationScoped
public class UpdateRoleApplicationRole {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;
    private final SecurityContextService securityContextService;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param roleRepository         the repository for accessing and saving roles
     * @param privilegeGuard         the guard for privilege escalation protection
     * @param auditService           the service for audit logging
     * @param securityContextService the service for security context invalidation
     */
    @Inject
    public UpdateRoleApplicationRole(RoleRepository roleRepository, UserRepository userRepository,
                                     PrivilegeEscalationGuard privilegeGuard,
                                     AuditService auditService, SecurityContextService securityContextService,
                                     EventPublisher eventPublisher) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
        this.securityContextService = securityContextService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes the command to update a role's ApplicationRole.
     *
     * @param command         the command containing the role ID and new ApplicationRole
     * @param securityContext the security context of the actor performing the operation
     * @throws RoleNotFoundException if the role does not exist
     * @throws org.access.domain.exception.InsufficientPrivilegeException if actor is not TIER_0
     */
    @Transactional
    public void execute(UpdateRoleApplicationRoleCommand command, SecurityContext securityContext) {
        // Security: Only TIER_0 can update ApplicationRole
        privilegeGuard.validateMinimumTier(securityContext.actorTier(), SecurityTier.TIER_0);

        // Load role
        Role role = roleRepository.findById(new RoleId(command.roleId()))
            .orElseThrow(() -> new RoleNotFoundException(command.roleId()));

        // Capture old value for audit and event
        ApplicationRole oldApplicationRole = role.getApplicationRole();
        RoleId roleId = role.id();

        // Update ApplicationRole
        role.setApplicationRole(command.newApplicationRole());

        // Persist
        roleRepository.update(role);

        // Audit
        auditService.record(
            securityContext,
            "UPDATE_ROLE_APPLICATION_ROLE",
            "ROLE",
            role.id().id().toString(),
            "{\"roleCode\": \"" + role.code().value() + "\", \"oldApplicationRole\": \"" + oldApplicationRole + "\"}",
            "{\"roleCode\": \"" + role.code().value() + "\", \"newApplicationRole\": \"" + command.newApplicationRole() + "\"}"
        );

        // Invalidate security context for all users with this role
        securityContextService.invalidateByRole(roleId);

        // Get all users affected by this role change
        Set<UUID> affectedUserIds = userRepository.findByRoleId(roleId)
            .stream()
            .map(user -> user.getId().id())
            .collect(Collectors.toSet());

        // Publish domain event for cross-BC consistency
        eventPublisher.publishAsync(new RoleApplicationRoleChangedEvent(
            null,  // eventId auto-generated
            roleId.id(),
            role.code().value(),
            oldApplicationRole,
            command.newApplicationRole(),
            affectedUserIds,
            securityContext.actorId().id(),
            null  // occurredAt auto-generated
        ));
    }

    /**
     * Exception thrown when a role is not found.
     */
    public static class RoleNotFoundException extends RuntimeException {
        public RoleNotFoundException(java.util.UUID roleId) {
            super("Role not found: " + roleId);
        }
    }
}
