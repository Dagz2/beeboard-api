package org.access.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.domain.model.*;
import org.access.domain.repository.UserRepository;
import org.access.infrastructure.entity.UserEntity;
import org.access.infrastructure.mapper.UserMapper;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA-based implementation of the {@link UserRepository} interface using Panache.
 *
 * <p>This class provides persistence logic for {@link User} aggregate roots,
 * delegating object-relational mapping to {@link UserMapper}. It also extends
 * {@link PanacheRepository} for convenient database access methods.</p>
 */
@ApplicationScoped
public class UserRepositoryImpl implements UserRepository, PanacheRepository<UserEntity> {

    /**
     * Retrieves a domain user by ID using Panache and maps it to a domain object.
     *
     * @param id the domain user ID
     * @return an {@link Optional} with the domain user if found
     */
    @Override
    public Optional<User> findById(UserId id) {
        return find("userId", id.id())
            .firstResultOptional()
            .map(UserMapper::toDomain);
    }

    /**
     * Finds a user by their email address.
     *
     * @param mail the email address value object
     * @return the user domain object if found
     */
    @Override
    public Optional<User> findByMail(EmailAddress mail) {
        return find("email", mail.value())
            .firstResultOptional()
            .map(UserMapper::toDomain);
    }

    /**
     * Finds a user by their full name.
     *
     * @param name the full name value object
     * @return the user domain object if found
     */
    @Override
    public Optional<User> findByName(FullName name) {
        return find("firstName = ?1 and lastName = ?2", name.firstName(), name.lastName())
            .firstResultOptional()
            .map(UserMapper::toDomain);
    }

    /**
     * Retrieves all users from the database and maps them to domain objects.
     *
     * @return list of all domain users
     */
    @Override
    public List<User> findAllUsers() {
        return findAll().stream()
            .map(UserMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all users assigned to a specific role from the database.
     *
     * @param roleId the domain role ID to search for
     * @return list of domain users with the specified role
     */
    @Override
    public List<User> findByRoleId(RoleId roleId) {
        return find("role.roleId", roleId.id()).stream()
            .map(UserMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Persists a new user to the database.
     *
     * @param user the user domain object to save
     */
    @Override
    @Transactional
    public void save(User user) {
        persist(UserMapper.toEntity(user));
    }

    /**
     * Updates an existing user in the database.
     *
     * @param user the user domain object to update
     */
    @Override
    @Transactional
    public void update(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        getEntityManager().merge(entity);
    }

    /**
     * Checks whether a user exists by ID.
     *
     * @param id the user ID to check
     * @return true if user exists, false otherwise
     */
    @Override
    public boolean existsById(UserId id) {
        return count("userId", id.id()) > 0;
    }

    /**
     * Checks whether any users are assigned to the given role ID.
     * This is used to verify referential integrity before role deletion.
     *
     * @param roleId the role ID to check for user assignments
     * @return true if there are users with this role, false otherwise
     */
    @Override
    public boolean existsByRoleId(RoleId roleId) {
        return count("role.roleId", roleId.id()) > 0;
    }

    /**
     * Deletes a user by its ID.
     *
     * @param id the ID of the user to delete
     */
    @Override
    @Transactional
    public void deleteById(UserId id) {
        delete("userId", id.id());
    }
}
