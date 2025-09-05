package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.role.UpdateRoleCommand;
import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleId;
import org.access.domain.model.RoleName;
import org.access.domain.repository.RoleRepository;

/**
 * Use case for updating a role's code and name.
 *
 * This command updates an existing role's code and name fields
 * and persists the changes using the {@link RoleRepository}.
 */
@ApplicationScoped
public class UpdateRole {

    private final RoleRepository roleRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param roleRepository the repository for accessing and saving roles
     */
    public UpdateRole(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Executes the command to update a role's code and name.
     *
     * @param command the command containing the role ID and new values
     * @throws IllegalArgumentException if the role does not exist
     */
    @Transactional
    public void execute(UpdateRoleCommand command) {
        Role role = roleRepository.findById(new RoleId(command.roleId()))
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + command.roleId()));

        role.updateCode(new RoleCode(command.code()));
        role.updateName(new RoleName(command.name()));
        
        roleRepository.update(role);
    }
}