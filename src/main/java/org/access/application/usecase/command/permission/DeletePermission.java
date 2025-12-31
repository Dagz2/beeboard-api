package org.access.application.usecase.command.permission;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.permission.DeletePermissionCommand;
import org.access.domain.model.Permission;
import org.access.domain.model.PermissionId;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.service.PrivilegeEscalationGuard;

/**
 * Use case for deleting an existing permission.
 *
 * This command removes a permission from the system based on its unique identifier.
 * System permissions (marked as isSystem=true) cannot be deleted.
 */
@ApplicationScoped
public class DeletePermission {

    private final PermissionRepository permissionRepository;
    private final PrivilegeEscalationGuard privilegeGuard;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param permissionRepository the repository responsible for accessing permissions
     * @param privilegeGuard       the guard for privilege escalation protection
     */
    @Inject
    public DeletePermission(PermissionRepository permissionRepository, PrivilegeEscalationGuard privilegeGuard) {
        this.permissionRepository = permissionRepository;
        this.privilegeGuard = privilegeGuard;
    }

    /**
     * Executes the deletion of a permission identified by its ID.
     *
     * @param command the command containing the permission ID to delete
     * @throws org.access.domain.exception.SystemResourceProtectedException if the permission is a system permission
     */
    @Transactional
    public void execute(DeletePermissionCommand command) {
        PermissionId permissionId = new PermissionId(command.permissionId());

        Permission permission = permissionRepository.findById(permissionId).orElse(null);

        if (permission != null) {
            // Validate not a system permission (system permissions cannot be deleted)
            privilegeGuard.validatePermissionDeletion(permission);
            permissionRepository.deleteById(permissionId);
        }
    }
}
