package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.domain.model.PermissionId;
import org.access.domain.model.Role;
import org.access.domain.model.RoleId;
import org.access.domain.repository.RoleRepository;
import org.access.application.service.SecurityContextService;

/**
 * Use case for revoking a permission from a role.
 *
 * This use case removes a {@link PermissionId} from a {@link Role}'s permission set
 * and persists the updated role using the {@link RoleRepository}.
 */
@ApplicationScoped
public class RevokePermissionFromRole {

    private final RoleRepository roleRepository;
    private final SecurityContextService securityContextService;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param roleRepository         the repository used to retrieve and persist roles
     * @param securityContextService the service for security context invalidation
     */
    @Inject
    public RevokePermissionFromRole(RoleRepository roleRepository, SecurityContextService securityContextService) {
        this.roleRepository = roleRepository;
        this.securityContextService = securityContextService;
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

        // Invalidate security context for all users with this role
        securityContextService.invalidateByRole(roleId);
    }
}
