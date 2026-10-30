package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.FarmDetailsUpdatedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when the descriptive data of a farm changes.
 */
public record FarmDetailsUpdatedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long farmId,
        Long ownerId,
        String name,
        String location,
        String department) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.farm.details-updated";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static FarmDetailsUpdatedIntegrationEvent from(FarmDetailsUpdatedEvent event) {
        return new FarmDetailsUpdatedIntegrationEvent(
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
