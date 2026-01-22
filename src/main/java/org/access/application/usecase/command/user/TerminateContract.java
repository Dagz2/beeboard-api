package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.TerminateContractCommand;
import org.access.application.service.AuditService;
import org.access.domain.event.UserContractTerminatedEvent;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.shared.event.EventPublisher;

import java.time.LocalDate;

/**
 * Use case for terminating a user's contract.
 *
 * This command updates the user's contract end date to today
 * and locks the account, then persists the change using {@link UserRepository}.
 *
 * Security validations:
 * <ul>
 *   <li>Actor cannot terminate their own contract (self-modification forbidden)</li>
 *   <li>Actor can only terminate contracts of users with lower privilege than their own tier</li>
 * </ul>
 */
@ApplicationScoped
public class TerminateContract {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param userRepository the repository for accessing and updating users
     * @param roleRepository the repository used to access roles for tier validation
     * @param privilegeGuard the guard for privilege escalation protection
     * @param auditService   the service for audit logging
     */
    @Inject
    public TerminateContract(UserRepository userRepository, RoleRepository roleRepository,
                             PrivilegeEscalationGuard privilegeGuard, AuditService auditService,
                             EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes the command to terminate a user's contract.
     * The user's account will also be locked.
     *
     * @param command         the command containing the user ID
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user does not exist
     * @throws org.access.domain.exception.SelfModificationForbiddenException if actor tries to terminate own contract
     * @throws org.access.domain.exception.PrivilegeEscalationException if actor tries to manage higher tier user
     */
    @Transactional
    public void execute(TerminateContractCommand command, SecurityContext securityContext) {
        UserId targetUserId = new UserId(command.userId());

        // Validate not self-modification
        privilegeGuard.validateNotSelfModification(securityContext.actorId(), targetUserId);

        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        // Load target user's role to check tier hierarchy
        Role targetRole = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new IllegalArgumentException("Role not found for user: " + command.userId()));

        // Validate tier hierarchy (actor can only manage lower privilege users)
        privilegeGuard.validateUserManagement(securityContext.actorTier(), targetRole.tier());

        String oldEndDate = user.getContractEndDate().date().toString();
        user.terminateContract();
        userRepository.update(user);

        // Audit the operation
        auditService.record(
            securityContext,
            "TERMINATE_CONTRACT",
            "USER",
            targetUserId.id().toString(),
            "{\"contractEndDate\": \"" + oldEndDate + "\", \"isLocked\": false}",
            "{\"contractEndDate\": \"today\", \"isLocked\": true}"
        );

        // Publish domain event for cross-BC consistency
        eventPublisher.publishAsync(new UserContractTerminatedEvent(
            null,  // eventId auto-generated
            command.userId(),
            user.getEmail().value(),
            LocalDate.now(),  // Contract terminated today
            securityContext.actorId().id(),
            null  // occurredAt auto-generated
        ));
    }
}
