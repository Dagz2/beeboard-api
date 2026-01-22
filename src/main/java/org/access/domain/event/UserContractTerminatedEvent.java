package org.access.domain.event;

import org.shared.event.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain event fired when a user's employment contract is terminated.
 *
 * Contract termination typically triggers an offboarding workflow:
 * - User account is automatically locked
 * - Access to all systems is revoked
 * - User data may need to be archived or transferred
 * - HR and IT processes are initiated
 *
 * This is distinct from simple account locking because it represents a business
 * event (end of employment) rather than an administrative action.
 *
 * Consumers should:
 * - Invalidate all caches for this user
 * - Archive user-owned resources (notes, documents)
 * - Reassign active work items
 * - Notify relevant stakeholders (HR, managers)
 * - Initiate compliance procedures (data retention, exit interview)
 *
 * Published by: TerminateContract use case
 * Consumed by: Note BC (archival), Project BC (reassignment), HR system, future BCs
 */
public record UserContractTerminatedEvent(
    UUID eventId,
    UUID userId,
    String email,
    LocalDate terminationDate,
    UUID terminatedBy,
    Instant occurredAt
) implements DomainEvent {

    /**
     * Compact constructor with automatic defaults for null values.
     */
    public UserContractTerminatedEvent {
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
        if (terminationDate == null) {
            throw new IllegalArgumentException("terminationDate cannot be null");
        }
        if (terminatedBy == null) {
            throw new IllegalArgumentException("terminatedBy cannot be null");
        }
    }

    @Override
    public String eventType() {
        return "UserContractTerminated";
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }

    /**
     * Checks if the termination date is in the past.
     *
     * @return true if termination date has already occurred
     */
    public boolean isEffective() {
        return terminationDate.isBefore(LocalDate.now()) ||
               terminationDate.isEqual(LocalDate.now());
    }

    /**
     * Checks if the termination is scheduled for the future.
     *
     * @return true if termination date is in the future
     */
    public boolean isScheduled() {
        return terminationDate.isAfter(LocalDate.now());
    }
}
