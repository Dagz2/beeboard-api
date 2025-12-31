package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.CreateUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.model.*;
import org.access.domain.repository.UserRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.service.PrivilegeEscalationGuard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Use case responsible for creating a new {@link User} in the system.
 *
 * This class encapsulates the application logic for creating users, ensuring
 * that all domain constraints are respected, and that the user is persisted
 * via the appropriate repository.
 *
 * Security validations:
 * <ul>
 *   <li>Actor can only create users with roles of lower privilege than their own tier</li>
 * </ul>
 */
@ApplicationScoped
public class CreateUser {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param userRepository the repository used to persist the new user
     * @param roleRepository the repository used to verify role existence and tier
     * @param privilegeGuard the guard for privilege escalation protection
     * @param auditService   the service for audit logging
     */
    @Inject
    public CreateUser(UserRepository userRepository, RoleRepository roleRepository,
                      PrivilegeEscalationGuard privilegeGuard, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
    }

    /**
     * Executes the use case to create a new user.
     *
     * @param command         the input data needed to create the user
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the role does not exist or input is invalid
     * @throws org.access.domain.exception.PrivilegeEscalationException if actor tries to assign higher tier role
     */
    @Transactional
    public void execute(CreateUserCommand command, SecurityContext securityContext) {
        RoleId roleId = new RoleId(command.roleId());

        // Load role and validate tier hierarchy
        Role targetRole = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + command.roleId()));

        // Validate tier hierarchy (actor can only create users with lower privilege roles)
        privilegeGuard.validateRoleAssignment(securityContext.actorTier(), targetRole);

        // Construct value objects from command input
        FullName fullName = new FullName(command.firstName(), command.lastName());
        EmailAddress email = new EmailAddress(command.email());
        Password password = Password.fromPlainText(command.password());
        ContractEndDate contractEndDate = new ContractEndDate(command.contractEndDate());

        // Create new user aggregate
        User user = User.create(fullName, email, password, roleId, contractEndDate);

        // Persist user
        userRepository.save(user);

        // Audit the operation
        auditService.record(securityContext, "CREATE_USER", "USER", user.getId().id().toString());
    }
}
