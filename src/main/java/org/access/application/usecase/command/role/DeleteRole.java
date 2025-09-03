package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.command.role.DeleteRoleCommand;
import org.access.domain.model.RoleId;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;

/**
 * Use case for deleting a role from the system.
 *
 * This command removes a role based on its unique identifier.
 * Before deletion, it validates that no users are assigned to this role
 * to maintain referential integrity.
 */
@ApplicationScoped
public class DeleteRole {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repositories.
     *
     * @param roleRepository the repository responsible for accessing roles
     * @param userRepository the repository for checking user assignments
     */
    public DeleteRole(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Executes the deletion of a role identified by its ID.
     *
     * @param command the command containing the role ID to delete
     * @throws IllegalArgumentException if the role does not exist
     * @throws IllegalStateException if users are still assigned to this role
     */
    public void execute(DeleteRoleCommand command) {
        RoleId roleId = new RoleId(command.roleId());
        
        // Verify role exists
        if (!roleRepository.existsById(roleId)) {
            throw new IllegalArgumentException("Role not found with ID: " + command.roleId());
        }

        // Check if any users are assigned to this role
        if (userRepository.existsByRoleId(roleId)) {
            throw new IllegalStateException("Cannot delete role: users are still assigned to this role");
        }

        roleRepository.deleteById(roleId);
    }
}