package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.ParcelDetailsUpdatedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when the descriptive data of a parcel changes.
 */
public record ParcelDetailsUpdatedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long parcelId,
        Long farmId,
        String name) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.parcel.details-updated";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static ParcelDetailsUpdatedIntegrationEvent from(ParcelDetailsUpdatedEvent event) {
        return new ParcelDetailsUpdatedIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.parcelId(),
                event.farmId(),
                event.name());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
