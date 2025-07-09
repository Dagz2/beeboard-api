package org.access.infrastructure.mapper;

import org.access.domain.model.*;
import org.access.infrastructure.entity.RoleEntity;
import org.access.infrastructure.entity.AuditInfoEmbeddable;

import java.util.Set;
import java.util.stream.Collectors;

public class RoleMapper {

    /**
     * Maps a RoleEntity (JPA) to a domain Role.
     *
     * @param entity the RoleEntity to convert
     * @return the domain Role
     */
    public static Role toDomain(RoleEntity entity) {
        Set<PermissionId> permissionIds = entity.getPermissions()
            .stream()
            .map(p -> new PermissionId(p))
            .collect(Collectors.toSet());

        return new Role(
            new RoleId(entity.getRoleId()),
            new RoleCode(entity.getCode()),
            new RoleName(entity.getName()),
            permissionIds,
            new AuditInfo(entity.getAuditInfo().getCreatedAt(), entity.getAuditInfo().getUpdatedAt())
        );
    }

    /**
     * Maps a domain Role to a RoleEntity.
     *
     * @param role the domain Role to convert
     * @param permissions the set of PermissionEntity corresponding to role.permissions()
     * @return the RoleEntity ready to persist
     */
    public static RoleEntity toEntity(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setRoleId(role.id().id());
        entity.setCode(role.code().value());
        entity.setName(role.name().value());
        AuditInfoEmbeddable audit = new AuditInfoEmbeddable();
        audit.setCreatedAt(role.auditInfo().getCreatedAt());
        audit.setUpdatedAt(role.auditInfo().getUpdatedAt());
        entity.setAuditInfo(audit);
        entity.setPermissions(role.permissions()
            .stream()
            .map(permissionId -> permissionId.id())
            .collect(Collectors.toSet()));
        return entity;
    }
}
