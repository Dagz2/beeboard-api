package org.access.infrastructure.mapper;

import org.access.domain.model.*;
import org.access.infrastructure.entity.PermissionEntity;

public class PermissionMapper {

    /**
     * Converts a JPA {@link PermissionEntity} to a domain {@link Permission}.
     *
     * @param entity the PermissionEntity to convert (must not be null)
     * @return the corresponding domain Permission
     */
    public static Permission toDomain(PermissionEntity entity) {
        return new Permission(
            new PermissionId(entity.getPermissionId()),
            new PermissionCode(entity.getCode()),
            new PermissionName(entity.getName()),
            entity.getDescription()
        );
    }
    
    /**
     * Converts a domain {@link Permission} to a JPA {@link PermissionEntity}.
     *
     * @param permission the domain Permission to convert (must not be null)
     * @return the corresponding JPA entity
     */
    public static PermissionEntity toEntity(Permission permission) {
        PermissionEntity entity = new PermissionEntity();
        entity.setPermissionId(permission.id().id());
        entity.setCode(permission.code().value());
        entity.setName(permission.name().value());
        entity.setDescription(permission.description());
        return entity;
    }
}
