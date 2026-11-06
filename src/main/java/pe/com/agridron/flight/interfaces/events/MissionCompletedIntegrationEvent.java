package pe.com.agridron.flight.interfaces.events;

import pe.com.agridron.flight.domain.events.MissionCompletedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when a mission is completed, carrying its result so that
 * Analytics and Inventory can react without querying Flight Operations.
 */
public record MissionCompletedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long missionId,
        Long parcelId,
        Long droneId,
        Long agrochemicalId,
        Long operatorId,
        Double treatedArea,
        Double appliedVolume) implements IntegrationEvent {

    public static final String EVENT_TYPE = "flight.mission.completed";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static MissionCompletedIntegrationEvent from(MissionCompletedEvent event) {
        return new MissionCompletedIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.missionId(),
                event.parcelId(),
                event.droneId(),
                event.agrochemicalId(),
                event.operatorId(),
                event.treatedArea(),
                event.appliedVolume());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
