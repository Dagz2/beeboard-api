package org.access.application.usecase.query;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.query.PermissionDto;
import org.access.application.mapper.PermissionMapper;
import org.access.domain.model.Permission;
import org.access.domain.model.PermissionCode;
import org.access.domain.model.PermissionId;
import org.access.domain.repository.PermissionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for querying {@link Permission} data.
 *
 * <p>This service exposes read-only operations for retrieving permissions.
 * It transforms domain objects into {@link PermissionDto} to be consumed
 * by the application layer or external clients.</p>
 */
@ApplicationScoped
public class PermissionQueryService {

    private final PermissionRepository permissionRepository;

    /**
     * Constructs the query service with a reference to the repository.
     *
     * @param permissionRepository repository for accessing permission aggregates
     */
    public PermissionQueryService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Retrieves all permissions from the system.
     *
     * @return a list of {@link PermissionDto} representing all permissions
     */
    public List<PermissionDto> findAllPermissions() {
        return permissionRepository.findAllPermissions().stream()
            .map(PermissionMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a permission by its unique identifier.
     *
     * @param id the permission ID
     * @return an {@link Optional} containing the {@link PermissionDto} if found
     */
    public Optional<PermissionDto> findById(UUID id) {
        return permissionRepository.findById(new PermissionId(id))
            .map(PermissionMapper::toDto);
    }

    /**
     * Retrieves a permission by its functional code.
     *
     * @param code the permission code
     * @return an {@link Optional} containing the {@link PermissionDto} if found
     */
    public Optional<PermissionDto> findByCode(PermissionCode code) {
        return permissionRepository.findByCode(code)
            .map(PermissionMapper::toDto);
    }
}
