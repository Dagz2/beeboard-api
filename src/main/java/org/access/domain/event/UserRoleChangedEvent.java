package org.access.domain.event;

import org.shared.domain.ApplicationRole;
import org.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event fired when a user's role is changed.
 *
 * This is a critical event that affects authorization across all bounded contexts.
 * When a user's role changes, their permissions and access levels may change significantly.
 *
 * Consumers should:
 * - Invalidate permission caches
 * - Re-evaluate access to protected resources
 * - Log the change for audit purposes
 *
 * Published by: AssignNewRole use case
 * Consumed by: Note BC (cache invalidation), future BCs
 */
public record UserRoleChangedEvent(
    UUID eventId,
    UUID userId,
    UUID oldRoleId,
    UUID newRoleId,
    ApplicationRole oldApplicationRole,
    ApplicationRole newApplicationRole,
    UUID changedBy,
    Instant occurredAt
) implements DomainEvent {

    /**
     * Compact constructor with automatic defaults for null values.
     */
    public UserRoleChangedEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        // Validate non-null critical fields
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (oldRoleId == null) {
            throw new IllegalArgumentException("oldRoleId cannot be null");
        }
        if (newRoleId == null) {
            throw new IllegalArgumentException("newRoleId cannot be null");
        }
        if (oldApplicationRole == null) {
            throw new IllegalArgumentException("oldApplicationRole cannot be null");
        }
        if (newApplicationRole == null) {
            throw new IllegalArgumentException("newApplicationRole cannot be null");
        }
        if (changedBy == null) {
            throw new IllegalArgumentException("changedBy cannot be null");
        }
    }

    @Override
    public String eventType() {
        return "UserRoleChanged";
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    /**
     * Checks if this role change represents a privilege escalation.
     *
     * @return true if new role has higher privileges than old role
     */
    public boolean isEscalation() {
        return newApplicationRole.ordinal() < oldApplicationRole.ordinal();
    }

    /**
     * Checks if this role change represents a privilege degradation.
     *
     * @return true if new role has lower privileges than old role
     */
    public boolean isDegradation() {
        return newApplicationRole.ordinal() > oldApplicationRole.ordinal();
    }
}
