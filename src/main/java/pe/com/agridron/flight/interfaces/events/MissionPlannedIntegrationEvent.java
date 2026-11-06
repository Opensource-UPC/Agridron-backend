package pe.com.agridron.flight.interfaces.events;

import pe.com.agridron.flight.domain.events.MissionPlannedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Integration event published when a mission is planned.
 */
public record MissionPlannedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long missionId,
        Long parcelId,
        LocalDate requestedDate) implements IntegrationEvent {

    public static final String EVENT_TYPE = "flight.mission.planned";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static MissionPlannedIntegrationEvent from(MissionPlannedEvent event) {
        return new MissionPlannedIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.missionId(),
                event.parcelId(),
                event.requestedDate());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
