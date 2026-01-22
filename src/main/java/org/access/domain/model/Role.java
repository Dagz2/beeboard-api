package org.access.domain.model;

import org.shared.domain.ApplicationRole;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a role assigned to a user.
 * A role aggregates a set of permissions and has a unique identity.
 *
 * Each role is classified by a {@link SecurityTier} that determines its privilege level:
 * <ul>
 *   <li>TIER_0 - Critical roles (SUPER_ADMIN) with full system access</li>
 *   <li>TIER_1 - Sensitive roles (DIRECTOR) with elevated privileges</li>
 *   <li>TIER_2 - Standard roles (HR_MANAGER and others) with normal privileges</li>
 * </ul>
 *
 * System roles (marked as {@code isSystem = true}) are protected and cannot be deleted
 * or have their core properties modified except by SUPER_ADMIN.
 */
public class Role {

    private final RoleId id;
    private RoleCode code;
    private RoleName name;
    private final Set<PermissionId> permissions;
    private final AuditInfo auditInfo;
    private SecurityTier tier;
    private boolean isSystem;
    private ApplicationRole applicationRole;

    // -------------------- Creation --------------------

    /**
     * Constructs a new Role with full parameters.
     *
     * @param id              the role's unique identifier
     * @param code            the functional code of the role
     * @param name            the business name of the role
     * @param permissions     initial set of permissions (optional)
     * @param auditInfo       the audit information
     * @param tier            the security tier classification
     * @param isSystem        whether this is a system-protected role
     * @param applicationRole the application-level role for cross-BC communication
     * @throws NullPointerException if required parameters are null
     */
    public Role(RoleId id, RoleCode code, RoleName name, Set<PermissionId> permissions,
                AuditInfo auditInfo, SecurityTier tier, boolean isSystem, ApplicationRole applicationRole) {
        this.id = Objects.requireNonNull(id, "Role ID cannot be null.");
        this.code = Objects.requireNonNull(code, "Role code cannot be null.");
        this.name = Objects.requireNonNull(name, "Role name cannot be null.");
        this.permissions = permissions != null
            ? new HashSet<>(permissions)
            : new HashSet<>();
        this.auditInfo = Objects.requireNonNull(auditInfo, "AuditInfo cannot be null.");
        this.tier = Objects.requireNonNull(tier, "Security tier cannot be null.");
        this.isSystem = isSystem;
        this.applicationRole = Objects.requireNonNull(applicationRole, "ApplicationRole cannot be null.");
    }

    /**
     * Factory method to create a new standard Role (TIER_2, non-system).
     *
     * @param code the functional code of the role
     * @param name the business name of the role
     * @return a new Role instance with default tier (TIER_2), isSystem=false, and ApplicationRole.MEMBER
     */
    public static Role create(RoleCode code, RoleName name) {
        Instant now = Instant.now();
        AuditInfo auditInfo = AuditInfo.of(now);
        return new Role(RoleId.newId(), code, name, new HashSet<>(), auditInfo, SecurityTier.defaultTier(), false, ApplicationRole.MEMBER);
    }

    /**
     * Factory method to create a new Role with explicit tier, system flag, and application role.
     *
     * @param code            the functional code of the role
     * @param name            the business name of the role
     * @param tier            the security tier classification
     * @param isSystem        whether this is a system-protected role
     * @param applicationRole the application-level role for cross-BC communication
     * @return a new Role instance
     */
    public static Role create(RoleCode code, RoleName name, SecurityTier tier, boolean isSystem, ApplicationRole applicationRole) {
        Instant now = Instant.now();
        AuditInfo auditInfo = AuditInfo.of(now);
        return new Role(RoleId.newId(), code, name, new HashSet<>(), auditInfo, tier, isSystem, applicationRole);
    }

    // -------------------- Domain methods --------------------

    /**
     * Grants the given permission to this role.
     *
     * @param permissionId the permission to add
     * @return {@code true} if the permission was added, {@code false} if it was already present
     */
    public boolean grantPermission(PermissionId permissionId) {
        Objects.requireNonNull(permissionId, "Permission ID cannot be null.");
        Boolean added = permissions.add(permissionId);
        if(added) {
            auditInfo.touch();
        }
        return added;
    }

    /**
     * Revokes the given permission from this role.
     *
     * @param permissionId the permission to remove
     * @return {@code true} if the permission was removed, {@code false} if it was not present
     */
    public boolean revokePermission(PermissionId permissionId) {
        Objects.requireNonNull(permissionId, "Permission ID cannot be null.");
        Boolean removed = permissions.remove(permissionId);
        if(removed) {
            auditInfo.touch();
        }
        return removed;
    }

    /**
     * Checks whether the role has the given permission.
     *
     * @param permissionId the permission to check
     * @return {@code true} if the role contains the permission, {@code false} otherwise
     */
    public boolean hasPermission(PermissionId permissionId) {
        return permissions.contains(permissionId);
    }

    /**
     * Updates the role's code.
     *
     * @param newCode the new role code (must not be null)
     */
    public void updateCode(RoleCode newCode) {
        this.code = Objects.requireNonNull(newCode, "New role code cannot be null.");
        auditInfo.touch();
    }

    /**
     * Updates the role's name.
     *
     * @param newName the new role name (must not be null)
     */
    public void updateName(RoleName newName) {
        this.name = Objects.requireNonNull(newName, "New role name cannot be null.");
        auditInfo.touch();
    }

    // -------------------- Getters --------------------

    /**
     * Gets the ID of this role.
     *
     * @return the role's ID
     */
    public RoleId id() {
        return id;
    }

    /**
     * Gets the full code of this role.
     *
     * @return the role's code
     */
    public RoleName name() {
        return name;
    }

    /**
     * Gets the full code of this role.
     *
     * @return the role's code
     */
    public RoleCode code() {
        return code;
    }

    /**
     * Gets the audit info of this role.
     *
     * @return the role's audit info
     */
    public AuditInfo auditInfo() {
        return auditInfo;
    }

    /**
     * Returns the set of permission identifiers associated with this role.
     *
     * @return an unmodifiable set of {@link PermissionId} assigned to the role
     */
    public Set<PermissionId> permissions() {
        return Collections.unmodifiableSet(permissions);
    }

    /**
     * Gets the security tier of this role.
     *
     * @return the role's security tier
     */
    public SecurityTier tier() {
        return tier;
    }

    /**
     * Checks if this role is a system-protected role.
     * System roles (SUPER_ADMIN, DIRECTOR, HR_MANAGER) cannot be deleted
     * and their core properties cannot be modified except by SUPER_ADMIN.
     *
     * @return true if this is a system role
     */
    public boolean isSystem() {
        return isSystem;
    }

    /**
     * Gets the application-level role for cross-BC communication.
     *
     * @return the ApplicationRole enum value
     */
    public ApplicationRole getApplicationRole() {
        return applicationRole;
    }

    /**
     * Sets the application-level role for cross-BC communication.
     * Updates audit information when the role changes.
     *
     * @param applicationRole the new ApplicationRole (must not be null)
     */
    public void setApplicationRole(ApplicationRole applicationRole) {
        this.applicationRole = Objects.requireNonNull(applicationRole, "ApplicationRole cannot be null.");
        auditInfo.touch();
    }

    // -------------------- Overrides --------------------

    /**
     * Two Role instances are considered equal if they share the same business identifier (RoleId).
     * This ensures entity identity is based solely on the unique id, not on mutable attributes.
     *
     * @param o the object to compare with
     * @return true if both objects are the same instance or have equal RoleIds
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role other)) return false;
        return id.equals(other.id);
    }

    /**
     * Computes the hash code based solely on the RoleId to maintain consistency with equals().
     *
     * @return hash code derived from the RoleId
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns a string representation of the role, including its code.
     *
     * @return a string in the format {@code Role[CODE]}
     */
    @Override
    public String toString() {
        return "Role[" + code.value() + "]";
    }

}
