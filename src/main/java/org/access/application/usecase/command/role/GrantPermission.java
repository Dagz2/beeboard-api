package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.domain.model.PermissionId;
import org.access.domain.model.Role;
import org.access.domain.model.RoleId;
import org.access.domain.repository.RoleRepository;

/**
 * Use case for granting a permission to a role.
 *
 * This use case adds a {@link PermissionId} to a {@link Role}'s permission set
 * and persists the updated role using the {@link RoleRepository}.
 */
@ApplicationScoped
public class GrantPermission {

    private final RoleRepository roleRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param roleRepository the repository used to retrieve and persist roles
     */
    public GrantPermission(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Executes the addition of a permission to a role.
     *
     * @param roleId       the ID of the role to which the permission is granted
     * @param permissionId the ID of the permission to grant
     * @throws IllegalArgumentException if the role does not exist
     */
    public void execute(RoleId roleId, PermissionId permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId.id()));

        role.grantPermission(permissionId);

        roleRepository.save(role);
    }
}
