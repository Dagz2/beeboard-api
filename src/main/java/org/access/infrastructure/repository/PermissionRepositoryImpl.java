package org.access.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.domain.model.Permission;
import org.access.domain.model.PermissionCode;
import org.access.domain.model.PermissionId;
import org.access.domain.repository.PermissionRepository;
import org.access.infrastructure.entity.PermissionEntity;
import org.access.infrastructure.mapper.PermissionMapper;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA-based implementation of the {@link PermissionRepository} using Panache.
 *
 * <p>This class provides database operations for {@link Permission} aggregates,
 * abstracting infrastructure logic and translating entities using the {@link PermissionMapper}.</p>
 */
@ApplicationScoped
public class PermissionRepositoryImpl implements PermissionRepository, PanacheRepository<PermissionEntity> {

    /**
     * Retrieves a permission from the database by its unique identifier.
     *
     * @param id the domain permission ID
     * @return an optional domain {@link Permission} if found
     */
    @Override
    public Optional<Permission> findById(PermissionId id) {
        return find("permissionId", id.id())
            .firstResultOptional()
            .map(PermissionMapper::toDomain);
    }

    /**
     * Retrieves a permission from the database using its functional code.
     *
     * @param code the domain permission code
     * @return an optional domain {@link Permission} if found
     */
    @Override
    public Optional<Permission> findByCode(PermissionCode code) {
        return find("code", code.value())
            .firstResultOptional()
            .map(PermissionMapper::toDomain);
    }

    /**
     * Returns all permissions from the database, mapped to domain objects.
     *
     * @return list of all permissions
     */
    @Override
    public List<Permission> findAllPermissions() {
        return findAll().stream()
            .map(PermissionMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Persists or updates a {@link Permission} domain object in the database.
     *
     * @param permission the domain permission to save or update
     */
    @Override
    @Transactional
    public void save(Permission permission) {
        PermissionEntity entity = PermissionMapper.toEntity(permission);
        if (!existsById(permission.id())) {
            persist(entity);
        } else {
            getEntityManager().merge(entity);
        }
    }

    /**
     * Deletes a permission from the database by its ID.
     *
     * @param id the domain permission ID to delete
     */
    @Override
    @Transactional
    public void deleteById(PermissionId id) {
        delete("permissionId", id.id());
    }

    /**
     * Checks whether a permission with the given ID exists in the database.
     *
     * @param id the domain permission ID
     * @return true if it exists, false otherwise
     */
    @Override
    public boolean existsById(PermissionId id) {
        return count("permissionId", id.id()) > 0;
    }
}
