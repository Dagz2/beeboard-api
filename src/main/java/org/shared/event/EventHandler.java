package org.shared.event;

/**
 * Marker interface for event handlers.
 *
 * Event handlers process domain events and trigger side effects such as:
 * - Cache invalidation
 * - Sending notifications
 * - Updating read models
 * - Publishing to external systems
 *
 * Implementations should be:
 * - Idempotent: handling the same event multiple times should be safe
 * - Fast: avoid blocking operations in event handlers
 * - Resilient: handle errors gracefully without crashing
 *
 * @param <T> the type of event this handler processes
 */
public interface EventHandler<T extends DomainEvent> {

    /**
     * Handles the given domain event.
     *
     * This method should:
     * - Be idempotent (safe to call multiple times with same event)
     * - Not throw exceptions (catch and log errors internally)
     * - Execute quickly (delegate long-running work to async processors)
     *
     * @param event the event to handle (never null)
     */
    void handle(T event);
}
