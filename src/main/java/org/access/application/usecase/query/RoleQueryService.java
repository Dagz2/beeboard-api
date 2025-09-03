package org.access.application.usecase.query;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.query.RoleDto;
import org.access.application.mapper.RoleMapper;
import org.access.domain.model.PermissionId;
import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleId;
import org.access.domain.repository.RoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application service for querying {@link Role} data.
 *
 * <p>This service exposes read-only operations for retrieving roles and checking
 * permission membership. It maps domain objects to {@link RoleDto} for use in the
 * application layer.</p>
 */
@ApplicationScoped
public class RoleQueryService {

    private final RoleRepository roleRepository;

    /**
     * Constructs the query service with required dependencies.
     *
     * @param roleRepository the repository to retrieve domain role entities
     */
    public RoleQueryService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Retrieves all available roles in the system.
     *
     * @return list of {@link RoleDto} objects
     */
    public List<RoleDto> findAllRoles() {
        return roleRepository.findAllRoles().stream()
            .map(RoleMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a role by its unique identifier.
     *
     * @param roleId the ID of the role
     * @return an optional {@link RoleDto} if found
     */
    public Optional<RoleDto> findById(RoleId roleId) {
        return roleRepository.findById(roleId)
            .map(RoleMapper::toDto);
    }

    /**
     * Retrieves a role by its business code (e.g., "ADMIN").
     *
     * @param roleCode the functional code of the role
     * @return an optional {@link RoleDto} if found
     */
    public Optional<RoleDto> findByCode(RoleCode roleCode) {
        return roleRepository.findByCode(roleCode)
            .map(RoleMapper::toDto);
    }

    /**
     * Checks whether a role has a given permission.
     *
     * @param roleId        the ID of the role
     * @param permissionId  the ID of the permission to check
     * @return {@code true} if the role has the permission, {@code false} otherwise
     * @throws IllegalArgumentException if the role is not found
     */
    public boolean hasPermission(RoleId roleId, PermissionId permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId.id()));
        return role.hasPermission(permissionId);
    }
}
