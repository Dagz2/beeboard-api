package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.ExtendContractCommand;
import org.access.application.service.AuditService;
import org.access.domain.model.ContractEndDate;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;

/**
 * Use case for extending a user's contract.
 *
 * This command updates the user's contract end date
 * and persists the updated user through the {@link UserRepository}.
 *
 * Security validations:
 * <ul>
 *   <li>Actor cannot extend their own contract (self-modification forbidden)</li>
 *   <li>Actor can only extend contracts of users with lower privilege than their own tier</li>
 * </ul>
 */
@ApplicationScoped
public class ExtendContract {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param userRepository the repository for accessing and saving users
     * @param roleRepository the repository used to access roles for tier validation
     * @param privilegeGuard the guard for privilege escalation protection
     * @param auditService   the service for audit logging
     */
    @Inject
    public ExtendContract(UserRepository userRepository, RoleRepository roleRepository,
                          PrivilegeEscalationGuard privilegeGuard, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
    }

    /**
     * Executes the contract extension command for a given user.
     *
     * @param command         the command containing the user ID and new contract end date
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user does not exist or the new date is invalid
     * @throws org.access.domain.exception.SelfModificationForbiddenException if actor tries to extend own contract
     * @throws org.access.domain.exception.PrivilegeEscalationException if actor tries to manage higher tier user
     */
    @Transactional
    public void execute(ExtendContractCommand command, SecurityContext securityContext) {
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
        user.extendContract(new ContractEndDate(command.newEndDate()));
        userRepository.update(user);

        // Audit the operation
        auditService.record(
            securityContext,
            "EXTEND_CONTRACT",
            "USER",
            targetUserId.id().toString(),
            "{\"contractEndDate\": \"" + oldEndDate + "\"}",
            "{\"contractEndDate\": \"" + command.newEndDate().toString() + "\"}"
        );
    }
}
