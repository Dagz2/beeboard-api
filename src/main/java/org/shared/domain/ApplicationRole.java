package org.shared.domain;

/**
 * Published Language: Application-wide roles for cross-BC communication.
 *
 * This enum represents capability levels, not organizational hierarchy.
 * BC Access maps organizational roles (DIRECTOR, HR_MANAGER) to these.
 * Other BCs translate these to their local vocabulary (AccessLevel, etc).
 */
public enum ApplicationRole {

    /**
     * Full system access - technical administration.
     * Can perform any operation including system configuration.
     * Maps from: SUPER_ADMIN
     */
    ADMIN,

    /**
     * Read-all, supervisory access - direction level.
     * Can view everything but cannot modify resources they don't own.
     * Maps from: DIRECTOR
     */
    SUPERVISOR,

    /**
     * Resource management - project managers, team leads, HR.
     * Can manage projects, tasks, team members, and roles.
     * Maps from: PROJECT_MANAGER, TECH_LEAD, HR_MANAGER
     */
    MANAGER,

    /**
     * Standard user access - developers, regular employees.
     * Can work on assigned resources and create content.
     * Maps from: DEVELOPER, SENIOR_DEVELOPER
     */
    MEMBER,

    /**
     * Limited, read-only access - interns, new contractors.
     * Can view assigned resources but cannot create or modify.
     * Maps from: INTERN, EXTERNAL
     */
    GUEST;

    /**
     * Checks if this role is at least as privileged as the given role.
     * ADMIN > SUPERVISOR > MANAGER > MEMBER > GUEST
     */
    public boolean isAtLeast(ApplicationRole minRole) {
        return this.ordinal() <= minRole.ordinal();
    }

    /**
     * Checks if this role has management capabilities.
     */
    public boolean canManage() {
        return this == ADMIN || this == SUPERVISOR || this == MANAGER;
    }

    /**
     * Checks if this role can supervise (view all).
     */
    public boolean canSupervise() {
        return this == ADMIN || this == SUPERVISOR;
    }
}
