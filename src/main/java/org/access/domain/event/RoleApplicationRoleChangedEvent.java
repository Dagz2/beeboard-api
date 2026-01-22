package org.access.domain.event;

import org.shared.domain.ApplicationRole;
import org.shared.event.DomainEvent;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Domain event fired when a role's ApplicationRole is changed.
 *
 * This is a CRITICAL event because it affects ALL users assigned to this role.
 * When a role's ApplicationRole changes, the authorization level of potentially
 * many users changes simultaneously.
 *
 * Example: If the "Manager" role's ApplicationRole changes from MANAGER to MEMBER,
 * all managers in the system are downgraded to member-level access.
 *
 * Consumers should:
 * - Invalidate permission caches for ALL affected users
 * - Re-evaluate access to protected resources
 * - Notify affected users if applicable
 * - Log the change for compliance
 *
 * Published by: UpdateRoleApplicationRole use case
 * Consumed by: Note BC (bulk cache invalidation), future BCs
 */
public record RoleApplicationRoleChangedEvent(
    UUID eventId,
    UUID roleId,
    String roleCode,
    ApplicationRole oldApplicationRole,
    ApplicationRole newApplicationRole,
    Set<UUID> affectedUserIds,
    UUID changedBy,
    Instant occurredAt
) implements DomainEvent {

    /**
     * Compact constructor with automatic defaults for null values.
     */
    public RoleApplicationRoleChangedEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        // Validate non-null critical fields
        if (roleId == null) {
            throw new IllegalArgumentException("roleId cannot be null");
        }
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("roleCode cannot be null or blank");
        }
        if (oldApplicationRole == null) {
            throw new IllegalArgumentException("oldApplicationRole cannot be null");
        }
        if (newApplicationRole == null) {
            throw new IllegalArgumentException("newApplicationRole cannot be null");
        }
        if (affectedUserIds == null) {
            throw new IllegalArgumentException("affectedUserIds cannot be null");
        }
        if (changedBy == null) {
            throw new IllegalArgumentException("changedBy cannot be null");
        }
    }

    @Override
    public String eventType() {
        return "RoleApplicationRoleChanged";
    }

    @Override
    public UUID aggregateId() {
        return roleId;
    }

    /**
     * Returns the number of users affected by this role change.
     *
     * @return count of affected users
     */
    public int affectedUserCount() {
        return affectedUserIds.size();
    }

    /**
     * Checks if this change represents a privilege escalation for the role.
     *
     * @return true if new ApplicationRole has higher privileges
     */
    public boolean isEscalation() {
        return newApplicationRole.ordinal() < oldApplicationRole.ordinal();
    }

    /**
     * Checks if this change represents a privilege degradation for the role.
     *
     * @return true if new ApplicationRole has lower privileges
     */
    public boolean isDegradation() {
        return newApplicationRole.ordinal() > oldApplicationRole.ordinal();
    }
}
