package org.access.domain.model;

import java.util.Objects;

/**
 * Aggregate Root representing a system permission.
 * 
 * A {@code Permission} defines an atomic capability that can be granted to roles.
 * It has a stable business identity and can be created, modified, or deleted independently.
 *
 * This aggregate is used to manage the global permission catalog and can be referenced
 * by other aggregates (such as {@code Role}) via {@link PermissionId}.
 */
public class Permission {

    private final PermissionId id;
    private PermissionCode code;
    private PermissionName name;
    private String description;

    // -------------------- Creation --------------------

    /**
     * Creates a new Permission.
     *
     * @param id          the unique identifier of the permission
     * @param code        the functional code of the permission (e.g. "VIEW_TASKS")
     * @param description a human-readable description of what this permission allows
     * @throws NullPointerException if any parameter is null
     */
    public Permission(PermissionId id, PermissionCode code, PermissionName name, String description) {
        this.id = Objects.requireNonNull(id, "Permission ID cannot be null.");
        this.code = Objects.requireNonNull(code, "Permission code cannot be null.");
        this.name = Objects.requireNonNull(name, "Permission name cannot be null.");
        this.description = Objects.requireNonNull(description, "Permission description cannot be null.");
    }

    /**
     * Factory method to create a new Permission
     *
     * @param code the functional code of the permission (e.g. "VIEW_TASKS")
     * @param description a human-readable description of what this permission allows
     * @return a new Permission instance
     */
    public static Permission create(PermissionCode code, PermissionName name, String description) {
        return new Permission(PermissionId.newId(), code, name, description);
    }

    // -------------------- Domain methods --------------------

    /**
     * Updates the permission's code.
     *
     * @param newCode the new permission code (must not be null)
     */
    public void updateCode(PermissionCode newCode) {
        this.code = Objects.requireNonNull(newCode, "New permission code cannot be null.");
    }

    /**
     * Updates the permission's name.
     *
     * @param newName the new permission name (must not be null)
     */
    public void updateName(PermissionName newName) {
        this.name = Objects.requireNonNull(newName, "New permission name cannot be null.");
    }

    /**
     * Updates the permission's description.
     *
     * @param newDescription the new permission description (must not be null)
     */
    public void updateDescription(String newDescription) {
        this.description = Objects.requireNonNull(newDescription, "New permission description cannot be null.");
    }

    // -------------------- Getters --------------------

    /**
     * Gets the ID of this permission.
     *
     * @return the permission's ID
     */
    public PermissionId id() {
        return id;
    }

    /**
     * Gets the code of this permission.
     *
     * @return the permission's code
     */
    public PermissionCode code() {
        return code;
    }

    /**
     * Gets the name of this permission.
     *
     * @return the permission's name
     */
    public PermissionName name() {
        return name;
    }

    /**
     * Gets the description of this permission.
     *
     * @return the permission's description
     */
    public String description() {
        return description;
    }

    // -------------------- Overrides --------------------

    /**
     * Two Permission instances are considered equal if they share the same business identifier (PermissionId).
     * This ensures entity identity is based solely on the unique id, not on mutable attributes.
     *
     * @param o the object to compare with
     * @return true if both objects are the same instance or have equal PermissionIds
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission other)) return false;
        return id.equals(other.id);
    }

    /**
     * Computes the hash code based solely on the PermissionId to maintain consistency with equals().
     *
     * @return hash code derived from the PermissionId
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns a string representation of the permission, including its code.
     *
     * @return a string in the format {@code Permission[CODE]}
     */
    @Override
    public String toString() {
        return "Permission[" + code.value() + "]";
    }
}