package org.shared.event;

/**
 * Abstraction for publishing domain events.
 *
 * This interface decouples event emission from the transport mechanism,
 * allowing the application to switch between CDI Events (in-process),
 * Kafka, RabbitMQ, or other messaging systems without changing business logic.
 *
 * Implementations should ensure:
 * - Thread safety for concurrent publications
 * - Proper error handling and logging
 * - Transaction awareness (events should only be published on successful commit)
 */
public interface EventPublisher {

    /**
     * Publishes an event synchronously.
     *
     * The event will be processed by all observers before this method returns.
     * Use this for critical events where you need immediate processing and
     * feedback about success/failure.
     *
     * @param <T> the type of event
     * @param event the event to publish (must not be null)
     * @throws IllegalArgumentException if event is null
     */
    <T extends DomainEvent> void publish(T event);

    /**
     * Publishes an event asynchronously.
     *
     * The event will be processed by observers asynchronously, and this method
     * returns immediately without waiting for processing to complete.
     * Use this for non-critical events where immediate processing is not required.
     *
     * @param <T> the type of event
     * @param event the event to publish (must not be null)
     * @throws IllegalArgumentException if event is null
     */
    <T extends DomainEvent> void publishAsync(T event);
}
