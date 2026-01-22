package org.shared.infrastructure.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.shared.event.DomainEvent;
import org.shared.event.EventPublisher;

/**
 * CDI-based implementation of EventPublisher.
 *
 * This implementation uses Jakarta CDI Events to publish domain events in-process.
 * Events are delivered to observers within the same JVM, making this suitable for
 * modular monoliths where bounded contexts run in the same application.
 *
 * Key characteristics:
 * - Synchronous: events are processed in the same thread by default
 * - Asynchronous: events can be processed async using @ObservesAsync
 * - Transactional: events fire within transaction boundaries
 * - Type-safe: CDI ensures compile-time type checking
 *
 * This implementation can later be replaced with a Kafka-based publisher
 * without changing any business logic, thanks to the EventPublisher abstraction.
 */
@ApplicationScoped
public class CdiEventPublisher implements EventPublisher {

    private static final Logger LOG = Logger.getLogger(CdiEventPublisher.class);

    @Inject
    Event<DomainEvent> eventBus;

    /**
     * Publishes an event synchronously using CDI Event.fire().
     *
     * All @Observes methods will be called synchronously before this method returns.
     * This ensures immediate consistency but may impact performance if observers
     * perform heavy operations.
     *
     * @param <T> the type of event
     * @param event the event to publish
     * @throws IllegalArgumentException if event is null
     */
    @Override
    public <T extends DomainEvent> void publish(T event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        LOG.debugf("Publishing synchronous event: %s [eventId=%s, aggregateId=%s]",
            event.eventType(), event.eventId(), event.aggregateId());

        try {
            eventBus.fire(event);
            LOG.tracef("Successfully published event: %s [eventId=%s]",
                event.eventType(), event.eventId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish event: %s [eventId=%s]",
                event.eventType(), event.eventId());
            throw e;
        }
    }

    /**
     * Publishes an event asynchronously using CDI Event.fireAsync().
     *
     * All @ObservesAsync methods will be called asynchronously in a managed thread pool.
     * This method returns immediately without waiting for observers to complete.
     * Prefer this for non-critical events where immediate consistency is not required.
     *
     * @param <T> the type of event
     * @param event the event to publish
     * @throws IllegalArgumentException if event is null
     */
    @Override
    public <T extends DomainEvent> void publishAsync(T event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        LOG.debugf("Publishing asynchronous event: %s [eventId=%s, aggregateId=%s]",
            event.eventType(), event.eventId(), event.aggregateId());

        try {
            eventBus.fireAsync(event);
            LOG.tracef("Successfully queued async event: %s [eventId=%s]",
                event.eventType(), event.eventId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish async event: %s [eventId=%s]",
                event.eventType(), event.eventId());
            throw e;
        }
    }
}
