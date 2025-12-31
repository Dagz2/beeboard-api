package org.access.infrastructure.mapper;

import org.access.domain.model.AuditInfo;
import org.access.domain.model.PermissionId;
import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleId;
import org.access.domain.model.RoleName;
import org.access.domain.model.SecurityTier;
import org.access.infrastructure.entity.AuditInfoEmbeddable;
import org.access.infrastructure.entity.PermissionEntity;
import org.access.infrastructure.entity.RoleEntity;

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

        Set<PermissionId> permissions = entity.getPermissions().stream()
            .map(permissionEntity -> new PermissionId(permissionEntity.getPermissionId()))
            .collect(Collectors.toSet());

        return new Role(
            new RoleId(entity.getRoleId()),
            new RoleCode(entity.getCode()),
            new RoleName(entity.getName()),
            permissions,
            new AuditInfo(entity.getAuditInfo().getCreatedAt(), entity.getAuditInfo().getUpdatedAt()),
            SecurityTier.valueOf(entity.getTier()),
            entity.isSystem()
        );
    }
    /**
     * Maps a domain Role to a RoleEntity.
     *
     * @param role the domain Role to convert
     * @return the RoleEntity ready to persist
     */
    public static RoleEntity toEntity(Role role) {
        RoleEntity entity = new RoleEntity();

        // Set the role's unique identifier
        entity.setRoleId(role.id().id());

        // Set the business and display identifiers
        entity.setCode(role.code().value());
        entity.setName(role.name().value());

        // Set audit information (creation and update timestamps)
        AuditInfoEmbeddable audit = new AuditInfoEmbeddable();
        audit.setCreatedAt(role.auditInfo().getCreatedAt());
        audit.setUpdatedAt(role.auditInfo().getUpdatedAt());
        entity.setAuditInfo(audit);

        // Set security tier and system flag
        entity.setTier(role.tier().name());
        entity.setSystem(role.isSystem());

        // Map permission identifiers to lightweight PermissionEntity references
        Set<PermissionEntity> permissions = role.permissions().stream()
            .map(permissionId -> {
                PermissionEntity permission = new PermissionEntity();
                permission.setPermissionId(permissionId.id());
                return permission;
            })
            .collect(Collectors.toSet());
        entity.setPermissions(permissions);

        return entity;
    }
}
