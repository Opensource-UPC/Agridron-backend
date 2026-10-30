package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.ParcelRegisteredEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when a parcel is registered.
 */
public record ParcelRegisteredIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long parcelId,
        Long farmId,
        BigDecimal areaHectares) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.parcel.registered";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static ParcelRegisteredIntegrationEvent from(ParcelRegisteredEvent event) {
        return new ParcelRegisteredIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.parcelId(),
                event.farmId(),
                event.areaHectares());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
