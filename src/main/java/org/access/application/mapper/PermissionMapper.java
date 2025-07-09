package org.access.application.mapper;

import org.access.application.dto.query.PermissionDto;
import org.access.domain.model.Permission;

/**
 * Mapper responsible for converting {@link Permission} domain objects
 * into {@link PermissionDto} instances used in the application layer.
 *
 * <p>This abstraction isolates domain logic from external representations and
 * allows the application/query layer to expose only relevant information
 * in a serializable, client-friendly structure.</p>
 */
public class PermissionMapper {

    /**
     * Converts a domain {@link Permission} object into a {@link PermissionDto}.
     *
     * @param permission the domain Permission entity to map (must not be {@code null})
     * @return the corresponding {@link PermissionDto}
     * @throws IllegalArgumentException if {@code permission} is {@code null}
     */
    public static PermissionDto toDto(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        return new PermissionDto(
            permission.id().id(),
            permission.code().value(),
            permission.name().value(),
            permission.description()
        );
    }
}
