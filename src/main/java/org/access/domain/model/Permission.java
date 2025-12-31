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
 *
 * Each permission is classified by a {@link SecurityTier} that determines which roles
 * can grant or revoke it. System permissions (marked as {@code isSystem = true}) are
 * protected and can only be modified by SUPER_ADMIN (TIER_0).
 */
public class Permission {

    private final PermissionId id;
    private PermissionCode code;
    private PermissionName name;
    private String description;
    private SecurityTier tier;
    private boolean isSystem;

    // -------------------- Creation --------------------

    /**
     * Creates a new Permission with full parameters.
     *
     * @param id          the unique identifier of the permission
     * @param code        the functional code of the permission (e.g. "VIEW_TASKS")
     * @param name        the human-readable name of the permission
     * @param description a human-readable description of what this permission allows
     * @param tier        the security tier classification
     * @param isSystem    whether this is a system-protected permission
     * @throws NullPointerException if any required parameter is null
     */
    public Permission(PermissionId id, PermissionCode code, PermissionName name, String description,
                      SecurityTier tier, boolean isSystem) {
        this.id = Objects.requireNonNull(id, "Permission ID cannot be null.");
        this.code = Objects.requireNonNull(code, "Permission code cannot be null.");
        this.name = Objects.requireNonNull(name, "Permission name cannot be null.");
        this.description = Objects.requireNonNull(description, "Permission description cannot be null.");
        this.tier = Objects.requireNonNull(tier, "Security tier cannot be null.");
        this.isSystem = isSystem;
    }

    /**
     * Factory method to create a new standard Permission (TIER_2, non-system).
     *
     * @param code        the functional code of the permission (e.g. "VIEW_TASKS")
     * @param name        the human-readable name of the permission
     * @param description a human-readable description of what this permission allows
     * @return a new Permission instance with default tier (TIER_2) and isSystem=false
     */
    public static Permission create(PermissionCode code, PermissionName name, String description) {
        return new Permission(PermissionId.newId(), code, name, description, SecurityTier.defaultTier(), false);
    }

    /**
     * Factory method to create a new Permission with explicit tier and system flag.
     *
     * @param code        the functional code of the permission
     * @param name        the human-readable name of the permission
     * @param description a human-readable description of what this permission allows
     * @param tier        the security tier classification
     * @param isSystem    whether this is a system-protected permission
     * @return a new Permission instance
     */
    public static Permission create(PermissionCode code, PermissionName name, String description,
                                    SecurityTier tier, boolean isSystem) {
        return new Permission(PermissionId.newId(), code, name, description, tier, isSystem);
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

    /**
     * Gets the security tier of this permission.
     *
     * @return the permission's security tier
     */
    public SecurityTier tier() {
        return tier;
    }

    /**
     * Checks if this permission is a system-protected permission.
     * System permissions cannot be modified or deleted except by SUPER_ADMIN (TIER_0).
     *
     * @return true if this is a system permission
     */
    public boolean isSystem() {
        return isSystem;
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