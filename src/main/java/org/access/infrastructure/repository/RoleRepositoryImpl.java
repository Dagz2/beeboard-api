package org.access.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleId;
import org.access.domain.repository.RoleRepository;
import org.access.infrastructure.entity.RoleEntity;
import org.access.infrastructure.entity.UserEntity;
import org.access.infrastructure.mapper.RoleMapper;
import org.access.infrastructure.mapper.UserMapper;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA-based implementation of the {@link RoleRepository} using Panache.
 *
 * <p>This class provides persistence operations for {@link Role} aggregates,
 * while abstracting database logic through {@link PanacheRepository}. It uses
 * {@link RoleMapper} to convert between domain and persistence models.</p>
 */
@ApplicationScoped
public class RoleRepositoryImpl implements RoleRepository, PanacheRepository<RoleEntity> {

    /**
     * Retrieves a role from the database by its unique identifier.
     *
     * @param id the domain role ID
     * @return an optional domain {@link Role} if found
     */
    @Override
    public Optional<Role> findById(RoleId id) {
        return find("roleId", id.id())
            .firstResultOptional()
            .map(RoleMapper::toDomain);
    }

    /**
     * Retrieves a role from the database using its functional code.
     *
     * @param code the domain role code
     * @return an optional domain {@link Role} if found
     */
    @Override
    public Optional<Role> findByCode(RoleCode code) {
        return find("code", code.value())
            .firstResultOptional()
            .map(RoleMapper::toDomain);
    }

    /**
     * Returns all roles from the database, mapped to domain objects.
     *
     * @return list of all roles
     */
    @Override
    public List<Role> findAllRoles() {
        return findAll().stream()
            .map(RoleMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Persists or updates a {@link Role} domain object in the database.
     *
     * @param role the domain role to persist
     */
    @Override
    @Transactional
    public void save(Role role) {
        RoleEntity entity = RoleMapper.toEntity(role);
        if (!existsById(role.id())) {
            persist(entity);
        } else {
            getEntityManager().merge(entity);
        }
    }

    /**
     * Updates an existing user in the database.
     *
     * @param user the user domain object to update
     */
    @Override
    @Transactional
    public void update(Role role) {
        RoleEntity entity = RoleMapper.toEntity(role);
        getEntityManager().merge(entity);
    }


    /**
     * Deletes a role from the database by its ID.
     *
     * @param id the domain role ID to delete
     */
    @Override
    @Transactional
    public void deleteById(RoleId id) {
        delete("roleId", id.id());
    }

    /**
     * Checks whether a role with the given ID exists in the database.
     *
     * @param id the domain role ID
     * @return true if it exists, false otherwise
     */
    @Override
    public boolean existsById(RoleId id) {
        return count("roleId", id.id()) > 0;
    }

}
