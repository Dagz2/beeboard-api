package org.access.application.usecase.command.permission;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.command.permission.UpdatePermissionCommand;
import org.access.domain.model.Permission;
import org.access.domain.model.PermissionCode;
import org.access.domain.model.PermissionId;
import org.access.domain.model.PermissionName;
import org.access.domain.repository.PermissionRepository;

/**
 * Use case for updating an existing permission.
 *
 * This use case handles the modification of a permission's properties
 * and persists the changes using the {@link PermissionRepository}.
 */
@ApplicationScoped
public class UpdatePermission {

    private final PermissionRepository permissionRepository;

    /**
     * Constructs the use case with the required permission repository.
     *
     * @param permissionRepository the repository used to access and update permissions
     */
    public UpdatePermission(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Executes the update of an existing permission based on the given command.
     *
     * @param command the input containing the permission ID and updated fields
     * @throws IllegalArgumentException if the permission does not exist
     */
    public void execute(UpdatePermissionCommand command) {
        PermissionId permissionId = new PermissionId(command.permissionId());
        
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + command.permissionId()));

        permission.updateCode(new PermissionCode(command.code()));
        permission.updateName(new PermissionName(command.name()));
        permission.updateDescription(command.description());

        permissionRepository.save(permission);
    }
}