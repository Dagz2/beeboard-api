package org.access.domain.event;

import org.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when a user's first or last name is changed.
 * Note BC uses this to update author_view.full_name.
 */
public record UserFullNameChangedEvent(
    UUID eventId,
    UUID userId,
    String oldFirstName,
    String oldLastName,
    String newFirstName,
    String newLastName,
    UUID changedBy,
    Instant occurredAt
) implements DomainEvent {

    // Compact constructor with validation and defaults
    public UserFullNameChangedEvent {
        eventId = eventId != null ? eventId : UUID.randomUUID();
        occurredAt = occurredAt != null ? occurredAt : Instant.now();

        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        if (newFirstName == null || newFirstName.isBlank())
            throw new IllegalArgumentException("newFirstName cannot be null or blank");
        if (newLastName == null || newLastName.isBlank())
            throw new IllegalArgumentException("newLastName cannot be null or blank");
        if (changedBy == null) throw new IllegalArgumentException("changedBy cannot be null");
    }

    @Override
    public String eventType() {
        return "UserFullNameChanged";
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    /**
     * Helper method: returns the old full name (concatenated)
     */
    public String oldFullName() {
        if (oldFirstName == null || oldLastName == null) {
            return null;
        }
        return oldFirstName + " " + oldLastName;
    }

    /**
     * Helper method: returns the new full name (concatenated)
     */
    public String newFullName() {
        return newFirstName + " " + newLastName;
    }
}
