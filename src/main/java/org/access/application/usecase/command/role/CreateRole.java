package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.role.CreateRoleCommand;
import org.access.application.service.AuditService;
import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleName;
import org.access.domain.model.SecurityContext;
import org.access.domain.repository.RoleRepository;

/**
 * Use case for creating a new role in the system.
 *
 * This command handles the creation of a {@link Role} aggregate from the provided command,
 * and persists it using the {@link RoleRepository}.
 */
@ApplicationScoped
public class CreateRole {

    private final RoleRepository roleRepository;
    private final AuditService auditService;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param roleRepository the repository responsible for storing roles
     * @param auditService   the service for audit logging
     */
    @Inject
    public CreateRole(RoleRepository roleRepository, AuditService auditService) {
        this.roleRepository = roleRepository;
        this.auditService = auditService;
    }

    /**
     * Executes the command to create a new role.
     *
     * @param command         the command containing the new role data
     * @param securityContext the security context of the actor performing the operation
     */
    @Transactional
    public void execute(CreateRoleCommand command, SecurityContext securityContext) {
        Role role = Role.create(
            new RoleCode(command.code()),
            new RoleName(command.name()),
            command.tier(),
            command.isSystem(),
            command.applicationRole()
        );

        roleRepository.save(role);

        // Audit the operation
        auditService.record(
            securityContext,
            "CREATE_ROLE",
            "ROLE",
            role.id().id().toString(),
            null,
            "{\"code\": \"" + command.code() + "\", \"name\": \"" + command.name() + "\", \"tier\": \"" + command.tier() + "\", \"applicationRole\": \"" + command.applicationRole() + "\"}"
        );
    }
}
