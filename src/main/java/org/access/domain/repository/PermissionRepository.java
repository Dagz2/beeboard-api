package org.access.domain.repository;

import org.access.domain.model.Permission;
import org.access.domain.model.PermissionCode;
import org.access.domain.model.PermissionId;

import java.util.List;
import java.util.Optional;


/**
 * Repository interface for accessing and persisting {@link Permission} aggregates.
 *
 * This interface defines the contract for retrieving and storing Permissions
 * in a decoupled, infrastructure-agnostic way.
 */
public interface PermissionRepository {

    /**
     * Finds a Permission by its unique identifier.
     *
     * @param id the Permission ID
     * @return an {@link Optional} containing the Permission if found, or empty if not found
     */
    Optional<Permission> findById(PermissionId id);

    /**
     * Retrieves a Permission by its code.
     *
     * @param mail the Permission's code
     * @return an {@link Optional} containing the Permission if found, or empty if not found
     */
    Optional<Permission> findByCode(PermissionCode code);

    /**
     * Retrieves all available Permissions.
     *
     * @return a list of all {@link Permission} entities
     */
    List<Permission> findAllPermissions();

    /**
     * Persists the given Permission.
     * Handles both creation and update operations.
     *
     * @param Permission the Permission to save
     */
    void save(Permission Permission);

    /**
     * Deletes a Permission by its ID.
     *
     * @param id the ID of the Permission to delete
     */
    void deleteById(PermissionId id);

    /**
     * Checks whether a Permission exists with the given ID.
     *
     * @param id the Permission ID
     * @return {@code true} if the Permission exists, {@code false} otherwise
     */
    boolean existsById(PermissionId id);
}
