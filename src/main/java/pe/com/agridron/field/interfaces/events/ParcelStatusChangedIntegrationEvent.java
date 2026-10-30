package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.ParcelStatusChangedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when a parcel changes its operational status.
 *
 * <p>The status is exposed as its stable name so consumers do not depend on the Field
 * domain enum.</p>
 */
public record ParcelStatusChangedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long parcelId,
        Long farmId,
        String status) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.parcel.status-changed";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static ParcelStatusChangedIntegrationEvent from(ParcelStatusChangedEvent event) {
        return new ParcelStatusChangedIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.parcelId(),
                event.farmId(),
                event.status().name());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
