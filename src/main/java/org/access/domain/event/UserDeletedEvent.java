package org.access.domain.event;

import org.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event fired when a user is permanently deleted from the system.
 *
 * This is a CRITICAL event representing data loss. The user account and all
 * associated authentication data are removed. Other bounded contexts must handle
 * orphaned data (notes, comments, projects) appropriately.
 *
 * Note: Users can only be deleted if they are already locked, providing a
 * safety mechanism against accidental deletion.
 *
 * Consumers should:
 * - Invalidate all caches for this user
 * - Handle orphaned resources (anonymize, reassign, or delete)
 * - Comply with GDPR "right to be forgotten" if applicable
 * - Log the deletion for compliance and audit
 * - Notify dependent systems
 *
 * Published by: DeleteUser use case
 * Consumed by: Note BC (orphan handling), Project BC, future BCs
 */
public record UserDeletedEvent(
    UUID eventId,
    UUID userId,
    String email,
    String fullName,
    UUID deletedBy,
    Instant occurredAt
) implements DomainEvent {

    /**
     * Compact constructor with automatic defaults for null values.
     */
    public UserDeletedEvent {
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
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName cannot be null or blank");
        }
        if (deletedBy == null) {
            throw new IllegalArgumentException("deletedBy cannot be null");
        }
    }

    @Override
    public String eventType() {
        return "UserDeleted";
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }
}
