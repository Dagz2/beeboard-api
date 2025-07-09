package org.access.application.mapper;

import org.access.application.dto.query.AuditInfoDto;
import org.access.application.dto.query.RoleDto;
import org.access.domain.model.PermissionId;
import org.access.domain.model.Role;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class responsible for converting domain {@link Role} entities
 * into {@link RoleDto} representations used in the application/query layer.
 *
 * <p>This mapper abstracts domain complexity and prepares a lightweight,
 * serializable structure for external systems or UI consumption.</p>
 */
public class RoleMapper {

    /**
     * Converts a {@link Role} domain object into a {@link RoleDto} instance.
     *
     * <p>The mapping extracts core business values (id, code, name) as well as audit information and
     * permissions (converted into a set of UUIDs).</p>
     *
     * @param role the domain Role entity to map (must not be {@code null})
     * @return a fully populated {@link RoleDto} object
     * @throws IllegalArgumentException if {@code role} is {@code null}
     */
    public static RoleDto toDto(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        Set<UUID> permissionIds = role.permissions()
            .stream()
            .map(PermissionId::id)
            .collect(Collectors.toSet());

        return new RoleDto(
            role.id().id(),
            role.code().value(),
            role.name().value(),
            new AuditInfoDto(
                role.auditInfo().getCreatedAt(),
                role.auditInfo().getUpdatedAt()
            ),
            permissionIds
        );
    }
}
