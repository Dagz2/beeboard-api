package org.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in the system.
 *
 * Domain events represent significant occurrences in the domain that domain experts care about.
 * They are immutable facts about something that has happened in the past.
 *
 * This abstraction allows events to be transported via different mechanisms (CDI, Kafka, etc.)
 * without changing the event definitions themselves.
 */
public interface DomainEvent {

    /**
     * Unique identifier for this event instance.
     *
     * @return the event's unique identifier
     */
    UUID eventId();

    /**
     * Type of the event (e.g., "UserRoleChanged", "UserLocked").
     * Used for routing, logging, and metrics.
     *
     * @return the event type as a string
     */
    String eventType();

    /**
     * Timestamp when this event occurred.
     *
     * @return the instant when the event was created
     */
    Instant occurredAt();

    /**
     * Identifier of the aggregate that produced this event.
     * Typically the ID of the domain entity that changed.
     *
     * @return the aggregate's unique identifier
     */
    UUID aggregateId();
}
