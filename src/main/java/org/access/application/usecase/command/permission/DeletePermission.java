package org.access.application.usecase.command.permission;

import jakarta.enterprise.context.ApplicationScoped;

import org.access.application.dto.command.permission.DeletePermissionCommand;
import org.access.domain.model.PermissionId;
import org.access.domain.repository.PermissionRepository;
import org.hibernate.sql.Delete;

/**
 * Use case for deleting an existing permission.
 *
 * This command removes a permission from the system based on its unique identifier.
 * If the permission does not exist, no action is taken.
 */
@ApplicationScoped
public class DeletePermission {

    private final PermissionRepository permissionRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param permissionRepository the repository responsible for accessing permissions
     */
    public DeletePermission(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Executes the deletion of a permission identified by its ID.
     *
     * @param permissionId the unique identifier of the permission to delete
     */
    public void execute(DeletePermissionCommand command) {
        if (permissionRepository.existsById(new PermissionId(command.permissionId()))) {
            permissionRepository.deleteById(new PermissionId(command.permissionId()));
        }
        // Silently ignore if not found — optional behavior
    }
}
