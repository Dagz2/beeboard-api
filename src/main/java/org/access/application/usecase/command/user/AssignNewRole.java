package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.AssignNewRoleCommand;
import org.access.application.service.AuditService;
import org.access.domain.event.UserRoleChangedEvent;
import org.access.domain.model.Role;
import org.access.domain.model.RoleId;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.access.application.service.SecurityContextService;
import org.shared.event.EventPublisher;

/**
 * Use case for assigning a new role to a user.
 *
 * This command changes the user's current role by updating their
 * role reference in the domain model and persisting the change
 * using the {@link UserRepository}.
 *
 * Security validations:
 * <ul>
 *   <li>Actor cannot modify their own role (self-modification forbidden)</li>
 *   <li>Actor can only assign roles with lower privilege than their own tier</li>
 * </ul>
 */
@ApplicationScoped
public class AssignNewRole {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;
    private final SecurityContextService securityContextService;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param userRepository         the repository for accessing and saving users
     * @param roleRepository         the repository for accessing roles
     * @param privilegeGuard         the guard for privilege escalation protection
     * @param auditService           the service for audit logging
     * @param securityContextService the service for security context invalidation
     */
    @Inject
    public AssignNewRole(UserRepository userRepository, RoleRepository roleRepository,
                         PrivilegeEscalationGuard privilegeGuard, AuditService auditService,
                         SecurityContextService securityContextService,
                         EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
        this.securityContextService = securityContextService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes the command to assign a new role to a user with security validation.
     *
     * @param command contains the user ID and the new role ID
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user or role is not found
     * @throws org.access.domain.exception.SelfModificationForbiddenException if actor tries to modify own role
     * @throws org.access.domain.exception.PrivilegeEscalationException if actor tries to assign higher tier role
     */
    @Transactional
    public void execute(AssignNewRoleCommand command, SecurityContext securityContext) {
        UserId targetUserId = new UserId(command.userId());
        RoleId newRoleId = new RoleId(command.roleId());

        // Validate not self-modification
        privilegeGuard.validateNotSelfModification(securityContext.actorId(), targetUserId);

        // Load the target role to check tier
        Role targetRole = roleRepository.findById(newRoleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + command.roleId()));

        // Validate tier hierarchy (actor can only assign lower privilege roles)
        privilegeGuard.validateRoleAssignment(securityContext.actorTier(), targetRole);

        // Load and update the user
        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        // Capture old role details BEFORE modification for event
        RoleId oldRoleId = user.getRoleId();
        Role oldRole = roleRepository.findById(oldRoleId)
            .orElseThrow(() -> new IllegalArgumentException("Old role not found"));

        // Perform the role assignment
        user.assignRole(newRoleId);
        userRepository.update(user);

        // Audit the operation
        auditService.record(
            securityContext,
            "ASSIGN_ROLE",
            "USER",
            targetUserId.id().toString(),
            "{\"roleId\": \"" + oldRoleId.id().toString() + "\"}",
            "{\"roleId\": \"" + newRoleId.id().toString() + "\"}"
        );

        // Invalidate security context for this user
        securityContextService.invalidate(command.userId());

        // Publish domain event for cross-BC consistency
        eventPublisher.publishAsync(new UserRoleChangedEvent(
            null,  // eventId auto-generated
            command.userId(),
            oldRoleId.id(),
            newRoleId.id(),
            oldRole.getApplicationRole(),
            targetRole.getApplicationRole(),
            securityContext.actorId().id(),
            null  // occurredAt auto-generated
        ));
    }
}
