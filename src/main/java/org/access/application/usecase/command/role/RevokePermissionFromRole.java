package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.domain.model.PermissionId;
import org.access.domain.model.Role;
import org.access.domain.model.RoleId;
import org.access.domain.repository.RoleRepository;

/**
 * Use case for revoking a permission from a role.
 *
 * This use case removes a {@link PermissionId} from a {@link Role}'s permission set
 * and persists the updated role using the {@link RoleRepository}.
 */
@ApplicationScoped
public class RevokePermissionFromRole {

    private final RoleRepository roleRepository;

    /**
     * Constructs the use case with the required role repository.
     *
     * @param roleRepository the repository used to retrieve and persist roles
     */
    public RevokePermissionFromRole(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Executes the removal of a permission from a role.
     *
     * @param roleIdthe ID of the role from which the permission will be removed
     * @param permissionId the ID of the permission to revoke
     * @throws IllegalArgumentException if the role does not exist
     */
    @Transactional
    public void execute(RoleId roleId, PermissionId permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId.id()));

        role.revokePermission(permissionId);

        roleRepository.save(role);
    }
}
