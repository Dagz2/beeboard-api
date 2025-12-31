package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.UnlockUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;

/**
 * Use case for unlocking a user account.
 *
 * This command reactivates the user's account by setting the `isLocked` flag to false,
 * and persists the change using {@link UserRepository}.
 *
 * Security validations:
 * <ul>
 *   <li>Actor cannot unlock themselves (self-modification forbidden)</li>
 *   <li>Actor can only unlock users with lower privilege than their own tier</li>
 * </ul>
 */
@ApplicationScoped
public class UnlockUser {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param userRepository the repository for accessing and updating users
     * @param roleRepository the repository used to access roles for tier validation
     * @param privilegeGuard the guard for privilege escalation protection
     * @param auditService   the service for audit logging
     */
    @Inject
    public UnlockUser(UserRepository userRepository, RoleRepository roleRepository,
                      PrivilegeEscalationGuard privilegeGuard, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
    }

    /**
     * Executes the command to unlock a user account.
     *
     * @param command         the command containing the user ID
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user is not found
     * @throws org.access.domain.exception.SelfModificationForbiddenException if actor tries to unlock themselves
     * @throws org.access.domain.exception.PrivilegeEscalationException if actor tries to unlock higher tier user
     */
    @Transactional
    public void execute(UnlockUserCommand command, SecurityContext securityContext) {
        UserId targetUserId = new UserId(command.userId());

        // Validate not self-unlocking
        privilegeGuard.validateNotSelfModification(securityContext.actorId(), targetUserId);

        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        // Load target user's role to check tier hierarchy
        Role targetRole = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new IllegalArgumentException("Role not found for user: " + command.userId()));

        // Validate tier hierarchy (actor can only unlock lower privilege users)
        privilegeGuard.validateUserManagement(securityContext.actorTier(), targetRole.tier());

        user.unlock();
        userRepository.update(user);

        // Audit the operation
        auditService.record(
            securityContext,
            "UNLOCK_USER",
            "USER",
            command.userId().toString(),
            "{\"isLocked\": true}",
            "{\"isLocked\": false}"
        );
    }
}
