package org.access.domain.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Value object representing the security context of the current actor.
 * Contains all security-relevant information needed for authorization decisions.
 */
public class SecurityContext {

    private final UserId actorId;
    private final String actorEmail;
    private final RoleId roleId;
    private final RoleCode roleCode;
    private final SecurityTier actorTier;
    private final Set<PermissionCode> permissions;

    public SecurityContext(UserId actorId, String actorEmail, RoleId roleId, RoleCode roleCode,
                           SecurityTier actorTier, Set<PermissionCode> permissions) {
        this.actorId = Objects.requireNonNull(actorId, "Actor ID cannot be null");
        this.actorEmail = Objects.requireNonNull(actorEmail, "Actor email cannot be null");
        this.roleId = Objects.requireNonNull(roleId, "Role ID cannot be null");
        this.roleCode = Objects.requireNonNull(roleCode, "Role code cannot be null");
        this.actorTier = Objects.requireNonNull(actorTier, "Security tier cannot be null");
        this.permissions = permissions != null
            ? Set.copyOf(permissions)
            : Collections.emptySet();
    }

    /**
     * Gets the ID of the current actor.
     */
    public UserId actorId() {
        return actorId;
    }

    /**
     * Gets the email of the current actor.
     */
    public String actorEmail() {
        return actorEmail;
    }

    /**
     * Gets the role ID of the current actor.
     */
    public RoleId roleId() {
        return roleId;
    }

    /**
     * Gets the role code of the current actor.
     */
    public RoleCode roleCode() {
        return roleCode;
    }

    /**
     * Gets the security tier of the current actor.
     */
    public SecurityTier actorTier() {
        return actorTier;
    }

    /**
     * Gets the set of permission codes granted to the current actor.
     */
    public Set<PermissionCode> permissions() {
        return permissions;
    }

    /**
     * Checks if the actor has the specified permission.
     */
    public boolean hasPermission(PermissionCode permissionCode) {
        return permissions.contains(permissionCode);
    }

    /**
     * Checks if the actor has the specified permission by code string.
     */
    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
            .anyMatch(p -> p.value().equals(permissionCode));
    }

    /**
     * Checks if the actor is a SUPER_ADMIN (TIER_0).
     */
    public boolean isSuperAdmin() {
        return actorTier == SecurityTier.TIER_0;
    }

    /**
     * Checks if the actor can manage resources of the given tier.
     */
    public boolean canManageTier(SecurityTier targetTier) {
        return actorTier.canManage(targetTier);
    }

    @Override
    public String toString() {
        return "SecurityContext[actorId=" + actorId.id() +
               ", email=" + actorEmail +
               ", role=" + roleCode.value() +
               ", tier=" + actorTier.name() + "]";
    }
}
