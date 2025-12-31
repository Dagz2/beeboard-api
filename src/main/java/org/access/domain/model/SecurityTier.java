package org.access.domain.model;

/**
 * Represents the security classification tier for permissions and roles.
 *
 * The tier system implements a hierarchical security model where:
 * <ul>
 *   <li>{@link #TIER_0} - Critical operations reserved for SUPER_ADMIN (system management, audit)</li>
 *   <li>{@link #TIER_1} - Sensitive operations for DIRECTOR (user deletion, role management)</li>
 *   <li>{@link #TIER_2} - Standard operations for HR_MANAGER and others (user creation, basic tasks)</li>
 * </ul>
 *
 * Lower tier values indicate higher privileges. An actor can only assign roles or grant permissions
 * of a tier strictly greater than their own tier level.
 */
public enum SecurityTier {

    /**
     * Critical tier - Reserved for SUPER_ADMIN.
     * Includes: permission management, system role management, audit log access, critical role assignment.
     */
    TIER_0(0, "Critical"),

    /**
     * Sensitive tier - Reserved for DIRECTOR.
     * Includes: user management, user deletion, non-system role management, standard role assignment.
     */
    TIER_1(1, "Sensitive"),

    /**
     * Standard tier - For HR_MANAGER and operational roles.
     * Includes: user creation, basic role assignment, standard operations.
     */
    TIER_2(2, "Standard");

    private final int level;
    private final String description;

    SecurityTier(int level, String description) {
        this.level = level;
        this.description = description;
    }

    /**
     * Gets the numeric level of this tier.
     * Lower values indicate higher privileges.
     *
     * @return the tier level (0 = highest privilege, 2 = standard)
     */
    public int level() {
        return level;
    }

    /**
     * Gets the human-readable description of this tier.
     *
     * @return the tier description
     */
    public String description() {
        return description;
    }

    /**
     * Checks if this tier has higher or equal privileges than the specified tier.
     *
     * @param other the tier to compare against
     * @return true if this tier has higher or equal privileges (lower or equal level)
     */
    public boolean isAtLeast(SecurityTier other) {
        return this.level <= other.level;
    }

    /**
     * Checks if this tier has strictly higher privileges than the specified tier.
     *
     * @param other the tier to compare against
     * @return true if this tier has strictly higher privileges (lower level)
     */
    public boolean isHigherThan(SecurityTier other) {
        return this.level < other.level;
    }

    /**
     * Checks if an actor with this tier can manage resources of the target tier.
     * An actor can only manage resources of a tier strictly lower in privilege (higher level number).
     *
     * @param targetTier the tier of the resource to manage
     * @return true if the actor can manage resources of the target tier
     */
    public boolean canManage(SecurityTier targetTier) {
        return this.level < targetTier.level;
    }

    /**
     * Gets the default tier for new permissions and roles.
     *
     * @return TIER_2 (Standard)
     */
    public static SecurityTier defaultTier() {
        return TIER_2;
    }
}
