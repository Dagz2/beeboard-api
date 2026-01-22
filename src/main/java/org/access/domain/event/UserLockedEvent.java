package org.access.domain.event;

import org.shared.event.DomainEvent;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain event fired when a user account is locked by an administrator.
 *
 * A locked user cannot authenticate or perform any operations in the system.
 * This is typically done for security reasons (suspicious activity, policy violation)
 * or administrative purposes (temporary suspension).
 *
 * Consumers should:
 * - Invalidate permission and authentication caches
 * - Terminate active sessions for this user
 * - Block access to user-owned resources (notes, projects, etc.)
 * - Log the action for security audit
 *
 * Published by: LockUser use case
 * Consumed by: Note BC (cache invalidation), Session Manager, future BCs
 */
public record UserLockedEvent(
    UUID eventId,
    UUID userId,
    String email,
    UUID lockedBy,
    Optional<String> reason,
    Instant occurredAt
) implements DomainEvent {

    /**
     * Compact constructor with automatic defaults for null values.
     */
    public UserLockedEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        if (reason == null) {
            reason = Optional.empty();
        }
        // Validate non-null critical fields
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        if (lockedBy == null) {
            throw new IllegalArgumentException("lockedBy cannot be null");
        }
    }

    @Override
    public String eventType() {
        return "UserLocked";
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    /**
     * Checks if a reason was provided for the lock.
     *
     * @return true if a reason is present
     */
    public boolean hasReason() {
        return reason.isPresent();
    }
}
