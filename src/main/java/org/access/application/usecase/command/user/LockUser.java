package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.LockUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.event.UserLockedEvent;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.shared.event.EventPublisher;

import java.util.Optional;

/**
 * Use case for locking a user account.
 *
 * This command sets the user status to locked,
 * preventing authentication or system access.
 * The change is persisted through {@link UserRepository}.
 *
 * Security validations:
 * <ul>
 *   <li>Actor cannot lock themselves (self-modification forbidden)</li>
 *   <li>Actor can only lock users with lower privilege than their own tier</li>
 * </ul>
 */
@ApplicationScoped
public class LockUser {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param userRepository the repository used to access and update users
     * @param roleRepository the repository used to access roles for tier validation
     * @param privilegeGuard the guard for privilege escalation protection
     * @param auditService   the service for audit logging
     */
    @Inject
    public LockUser(UserRepository userRepository, RoleRepository roleRepository,
                    PrivilegeEscalationGuard privilegeGuard, AuditService auditService,
                    EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes the command to lock a user account.
     *
     * @param command         the command containing the user ID to lock
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user does not exist
     * @throws IllegalStateException if the user is already locked
     * @throws org.access.domain.exception.SelfModificationForbiddenException if actor tries to lock themselves
     * @throws org.access.domain.exception.PrivilegeEscalationException if actor tries to lock higher tier user
     */
    @Transactional
    public void execute(LockUserCommand command, SecurityContext securityContext) {
        UserId targetUserId = new UserId(command.userId());

        // Validate not self-locking
        privilegeGuard.validateNotSelfModification(securityContext.actorId(), targetUserId);

        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        // Load target user's role to check tier hierarchy
        Role targetRole = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new IllegalArgumentException("Role not found for user: " + command.userId()));

        // Validate tier hierarchy (actor can only lock lower privilege users)
        privilegeGuard.validateUserManagement(securityContext.actorTier(), targetRole.tier());

        user.lock();
        userRepository.update(user);

        // Audit the operation
        auditService.record(
            securityContext,
            "LOCK_USER",
            "USER",
            command.userId().toString(),
            "{\"isLocked\": false}",
            "{\"isLocked\": true}"
        );

        // Publish domain event for cross-BC consistency
        eventPublisher.publishAsync(new UserLockedEvent(
            null,  // eventId auto-generated
            command.userId(),
            user.getEmail().value(),
            securityContext.actorId().id(),
            Optional.empty(),  // reason not provided in command
            null  // occurredAt auto-generated
        ));
    }
}
