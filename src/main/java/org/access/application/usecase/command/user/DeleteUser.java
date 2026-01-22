package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.DeleteUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.event.UserDeletedEvent;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.shared.event.EventPublisher;

/**
 * Use case for permanently deleting a user from the system.
 *
 * This command performs a hard delete operation, completely removing the user
 * from the database. It validates that the user is locked before deletion
 * to ensure proper business rules are followed.
 *
 * Security validations:
 * <ul>
 *   <li>Actor cannot delete themselves (self-modification forbidden)</li>
 *   <li>Actor can only delete users with lower privilege than their own tier</li>
 * </ul>
 */
@ApplicationScoped
public class DeleteUser {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param userRepository the repository used to access and delete users
     * @param roleRepository the repository used to access roles for tier validation
     * @param privilegeGuard the guard for privilege escalation protection
     * @param auditService   the service for audit logging
     */
    @Inject
    public DeleteUser(UserRepository userRepository, RoleRepository roleRepository,
                      PrivilegeEscalationGuard privilegeGuard, AuditService auditService,
                      EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes the command to permanently delete a user.
     *
     * @param command         the command containing the user ID to delete
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user does not exist
     * @throws IllegalStateException if the user is not locked (must be locked before deletion)
     * @throws org.access.domain.exception.SelfModificationForbiddenException if actor tries to delete themselves
     * @throws org.access.domain.exception.PrivilegeEscalationException if actor tries to delete higher tier user
     */
    @Transactional
    public void execute(DeleteUserCommand command, SecurityContext securityContext) {
        UserId targetUserId = new UserId(command.userId());

        // Validate not self-deletion
        privilegeGuard.validateNotSelfModification(securityContext.actorId(), targetUserId);

        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        // Load target user's role to check tier hierarchy
        Role targetRole = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new IllegalArgumentException("Role not found for user: " + command.userId()));

        // Validate tier hierarchy (actor can only delete lower privilege users)
        privilegeGuard.validateUserManagement(securityContext.actorTier(), targetRole.tier());

        if (!user.isLocked()) {
            throw new IllegalStateException("User must be locked before deletion. User ID: " + command.userId());
        }

        // Capture user data BEFORE deletion for event
        String email = user.getEmail().value();
        String fullName = user.getFullName().toString();

        userRepository.deleteById(targetUserId);

        // Audit the operation
        auditService.record(
            securityContext,
            "DELETE_USER",
            "USER",
            targetUserId.id().toString(),
            "{\"email\": \"" + email + "\"}",
            null
        );

        // Publish domain event for cross-BC consistency
        eventPublisher.publishAsync(new UserDeletedEvent(
            null,  // eventId auto-generated
            command.userId(),
            email,
            fullName,
            securityContext.actorId().id(),
            null  // occurredAt auto-generated
        ));
    }
}
