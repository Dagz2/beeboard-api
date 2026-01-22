package org.shared.domain;

import java.util.UUID;

/**
 * Published Language: User context for cross-BC communication.
 *
 * This is the minimal information other Bounded Contexts need
 * to make authorization decisions. It's immutable and safe to
 * pass between contexts.
 *
 * BC Note and BC Project will translate this to their local vocabulary.
 */
public record UserContext(
    UUID userId,
    ApplicationRole applicationRole,
    AccountStatus status
) {

    /**
     * Validation in compact constructor.
     */
    public UserContext {
        if (userId == null) {       
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (applicationRole == null) {
            throw new IllegalArgumentException("applicationRole cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
    }

    /**
     * Checks if user has ADMIN or SUPERVISOR role.
     */
    public boolean canSupervise() {
        return applicationRole.canSupervise();
    }

    /**
     * Checks if user has ADMIN role.
     */
    public boolean isAdmin() {
        return applicationRole == ApplicationRole.ADMIN;
    }

    /**
     * Checks if user role is at least the given minimum.
     */
    public boolean isAtLeast(ApplicationRole minRole) {
        return applicationRole.isAtLeast(minRole);
    }

    /**
     * Checks if user has management capabilities.
     */
    public boolean canManage() {
        return applicationRole.canManage();
    }

    /**
     * Checks if account is active.
     */
    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    /**
     * Checks if user has GUEST role (limited access).
     */
    public boolean isGuest() {
        return applicationRole == ApplicationRole.GUEST;
    }

    /**
     * Checks if user can create content (not a guest, account active).
     */
    public boolean canCreateContent() {
        return isActive() && !isGuest();
    }
}
