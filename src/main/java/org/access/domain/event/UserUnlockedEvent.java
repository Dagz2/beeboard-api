package org.access.domain.event;

import org.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event fired when a user account is unlocked by an administrator.
 *
 * An unlocked user regains the ability to authenticate and perform operations
 * according to their role and permissions. This reverses a previous UserLockedEvent.
 *
 * Consumers should:
 * - Invalidate permission and authentication caches
 * - Allow authentication attempts
 * - Restore access to user-owned resources
 * - Log the action for audit purposes
 *
 * Published by: UnlockUser use case
 * Consumed by: Note BC (cache invalidation), future BCs
 */
public record UserUnlockedEvent(
    UUID eventId,
    UUID userId,
    String email,
    UUID unlockedBy,
    Instant occurredAt
) implements DomainEvent {

    /**
     * Compact constructor with automatic defaults for null values.
     */
    public UserUnlockedEvent {
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
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        if (unlockedBy == null) {
            throw new IllegalArgumentException("unlockedBy cannot be null");
        }
    }

    @Override
    public String eventType() {
        return "UserUnlocked";
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }
}
