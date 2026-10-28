package pe.com.agridron.shared.domain.events;

import java.time.Instant;

/**
 * Contract for events published across bounded contexts.
 *
 * <p>Integration events are the only supported mechanism for asynchronous communication
 * between bounded contexts. They live in the producer's {@code interfaces.events} package
 * and must expose a stable identifier, the occurrence timestamp and a type name.</p>
 */
public interface IntegrationEvent {

    /**
     * Unique identifier of this event instance.
     */
    String eventId();

    /**
     * Timestamp at which the event occurred.
     */
    Instant occurredOn();

    /**
     * Stable event type name (e.g. {@code mission.completed}).
     */
    String eventType();
}
