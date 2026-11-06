package pe.com.agridron.flight.interfaces.events;

import pe.com.agridron.flight.domain.events.MissionStatusChangedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when a mission moves to another lifecycle status.
 *
 * <p>Statuses are exposed as their stable names so consumers do not depend on the Flight
 * Operations domain model.</p>
 */
public record MissionStatusChangedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long missionId,
        Long parcelId,
        String previousStatus,
        String status) implements IntegrationEvent {

    public static final String EVENT_TYPE = "flight.mission.status-changed";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static MissionStatusChangedIntegrationEvent from(MissionStatusChangedEvent event) {
        return new MissionStatusChangedIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.missionId(),
                event.parcelId(),
                event.previousStatus().name(),
                event.status().name());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
