package org.access.application.usecase.command.permission;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.command.permission.CreatePermissionCommand;
import org.access.domain.model.Permission;
import org.access.domain.model.PermissionCode;
import org.access.domain.model.PermissionName;
import org.access.domain.repository.PermissionRepository;

/**
 * Use case for creating a new permission.
 *
 * This use case encapsulates the logic required to instantiate a new {@link Permission}
 * and persist it using the {@link PermissionRepository}.
 */
@ApplicationScoped
public class CreatePermission {

    private final PermissionRepository permissionRepository;

    /**
     * Constructs the use case with the required permission repository.
     *
     * @param permissionRepository the repository used to persist permissions
     */
    public CreatePermission(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Executes the creation of a new permission based on the given command.
     *
     * @param command the input containing all required permission fields
     */
    public void execute(CreatePermissionCommand command) {
        Permission permission = Permission.create(
            new PermissionCode(command.code()),
            new PermissionName(command.name()),
            command.description()
        );

        permissionRepository.save(permission);
    }
}
