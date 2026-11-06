package pe.com.agridron.flight.interfaces.events;

import pe.com.agridron.flight.domain.events.IncidentReportedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when an incident is reported during a mission.
 */
public record IncidentReportedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long missionId,
        Long parcelId,
        String type,
        String description,
        Instant incidentOccurredAt) implements IntegrationEvent {

    public static final String EVENT_TYPE = "flight.incident.reported";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static IncidentReportedIntegrationEvent from(IncidentReportedEvent event) {
        return new IncidentReportedIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.missionId(),
                event.parcelId(),
                event.type(),
                event.description(),
                event.occurredAt());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
