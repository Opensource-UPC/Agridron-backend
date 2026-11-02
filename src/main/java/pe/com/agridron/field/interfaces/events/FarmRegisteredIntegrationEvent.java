package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.FarmRegisteredEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when a farm is registered.
 *
 * <p>It exposes the farm descriptive data so other bounded contexts can react without
 * depending on the Field domain model.</p>
 */
public record FarmRegisteredIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long farmId,
        Long ownerId,
        String name,
        String location,
        String department) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.farm.registered";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static FarmRegisteredIntegrationEvent from(FarmRegisteredEvent event) {
        return new FarmRegisteredIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.farmId(),
                event.ownerId(),
                event.name(),
                event.location(),
                event.department());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
