package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.command.role.CreateRoleCommand;
import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleName;
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

    /**
     * Constructs the use case with the required repository.
     *
     * @param roleRepository the repository responsible for storing roles
     */
    public CreateRole(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Executes the command to create a new role.
     *
     * @param command the command containing the new role data
     */
    public void execute(CreateRoleCommand command) {
        Role role = Role.create(
            new RoleCode(command.code()),
            new RoleName(command.name())
        );

        roleRepository.save(role);
    }
}
