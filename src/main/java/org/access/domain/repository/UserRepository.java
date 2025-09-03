package org.access.domain.repository;

import org.access.domain.model.EmailAddress;
import org.access.domain.model.FullName;
import org.access.domain.model.RoleId;
import org.access.domain.model.User;
import org.access.domain.model.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing and persisting {@link User} aggregates.
 * 
 * This is a Domain-Driven Design (DDD) style repository, representing
 * the contract for retrieving and storing {@code User} aggregates without
 * exposing infrastructure details (like JPA or database logic).
 */
public interface UserRepository {

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the user ID
     * @return an {@link Optional} containing the user if found, or empty if not found
     */
    Optional<User> findById(UserId id);

    /**
     * Retrieves a user by their mail.
     *
     * @param mail the user's mail
     * @return an {@link Optional} containing the user if found, or empty if not found
     */
    Optional<User> findByMail(EmailAddress mail);

    /**
     * Retrieves a user by their full name.
     *
     * @param mail the user's full name
     * @return an {@link Optional} containing the user if found, or empty if not found
     */
    Optional<User> findByName(FullName name);

    /**
     * Retrieves all customers.
     *
     * @return a list of all {@link User} entities
     */
    List<User> findAllUsers();

    /**
     * Retrieves all users assigned to a specific role.
     *
     * @param roleId the role ID to search for
     * @return a list of {@link User} entities with the specified role
     */
    List<User> findByRoleId(RoleId roleId);

    /**
     * Saves the given user aggregate.
     * 
     *
     * @param user the user to persist
     */
    void save(User user);

    /**
     * Update the given user aggregate.
     * 
     *
     * @param user the user to update
     */
    void update(User user);

    /**
     * Checks whether a user exists with the given ID.
     *
     * @param id the user ID
     * @return true if the user exists, false otherwise
     */
    boolean existsById(UserId id);

    /**
     * Checks whether any users are assigned to the given role ID.
     * This is typically used to verify referential integrity before role deletion.
     *
     * @param roleId the role ID to check for user assignments
     * @return {@code true} if there are users with this role, {@code false} otherwise
     */
    boolean existsByRoleId(RoleId roleId);

    /**
     * Deletes a user by its ID.
     *
     * @param id the ID of the user to delete
     */
    void deleteById(UserId id);

}