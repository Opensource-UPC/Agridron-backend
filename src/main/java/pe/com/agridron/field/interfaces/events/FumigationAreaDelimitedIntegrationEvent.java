package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.FumigationAreaDelimitedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when a fumigation area is delimited or its boundary changes.
 */
public record FumigationAreaDelimitedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long fumigationAreaId,
        Long parcelId,
        BigDecimal areaHectares) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.fumigation-area.delimited";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static FumigationAreaDelimitedIntegrationEvent from(FumigationAreaDelimitedEvent event) {
        return new FumigationAreaDelimitedIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.fumigationAreaId(),
                event.parcelId(),
                event.areaHectares());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
