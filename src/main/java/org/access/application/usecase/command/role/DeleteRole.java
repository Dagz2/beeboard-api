package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.role.DeleteRoleCommand;
import org.access.domain.model.Role;
import org.access.domain.model.RoleId;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.domain.service.PrivilegeEscalationGuard;

/**
 * Use case for deleting a role from the system.
 *
 * This command removes a role based on its unique identifier.
 * Before deletion, it validates that:
 * <ul>
 *   <li>No users are assigned to this role</li>
 *   <li>The role is not a system-protected role</li>
 * </ul>
 */
@ApplicationScoped
public class DeleteRole {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PrivilegeEscalationGuard privilegeGuard;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param roleRepository the repository responsible for accessing roles
     * @param userRepository the repository for checking user assignments
     * @param privilegeGuard the guard for privilege escalation protection
     */
    @Inject
    public DeleteRole(RoleRepository roleRepository, UserRepository userRepository,
                      PrivilegeEscalationGuard privilegeGuard) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.privilegeGuard = privilegeGuard;
    }

    /**
     * Executes the deletion of a role identified by its ID.
     *
     * @param command the command containing the role ID to delete
     * @throws IllegalArgumentException if the role does not exist
     * @throws IllegalStateException if users are still assigned to this role
     * @throws org.access.domain.exception.SystemResourceProtectedException if the role is a system role
     */
    @Transactional
    public void execute(DeleteRoleCommand command) {
        RoleId roleId = new RoleId(command.roleId());

        // Load the role to verify existence and check system flag
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + command.roleId()));

        // Validate not a system role (system roles cannot be deleted)
        privilegeGuard.validateRoleDeletion(role);

        // Check if any users are assigned to this role
        if (userRepository.existsByRoleId(roleId)) {
            throw new IllegalStateException("Cannot delete role: users are still assigned to this role");
        }

        roleRepository.deleteById(roleId);
    }
}