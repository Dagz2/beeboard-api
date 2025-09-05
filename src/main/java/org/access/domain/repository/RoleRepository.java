package org.access.domain.repository;

import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleId;

import java.util.List;
import java.util.Optional;


/**
 * Repository interface for accessing and persisting {@link Role} aggregates.
 *
 * This interface defines the contract for retrieving and storing roles
 * in a decoupled, infrastructure-agnostic way.
 */
public interface RoleRepository {

    /**
     * Finds a role by its unique identifier.
     *
     * @param id the role ID
     * @return an {@link Optional} containing the role if found, or empty if not found
     */
    Optional<Role> findById(RoleId id);

    /**
     * Retrieves a role by its code.
     *
     * @param mail the role's code
     * @return an {@link Optional} containing the role if found, or empty if not found
     */
    Optional<Role> findByCode(RoleCode code);

    /**
     * Retrieves all available roles.
     *
     * @return a list of all {@link Role} entities
     */
    List<Role> findAllRoles();

    /**
     * Persists the given role.
     * Handles both creation and update operations.
     *
     * @param role the role to save
     */
    void save(Role role);

    /**
     * Update the given user aggregate.
     * 
     *
     * @param user the user to update
     */
    void update(Role role);

    /**
     * Deletes a role by its ID.
     *
     * @param id the ID of the role to delete
     */
    void deleteById(RoleId id);

    /**
     * Checks whether a role exists with the given ID.
     *
     * @param id the role ID
     * @return {@code true} if the role exists, {@code false} otherwise
     */
    boolean existsById(RoleId id);

}
