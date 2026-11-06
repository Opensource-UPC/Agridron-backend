package pe.com.agridron.flight.domain.events;

import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;

/**
 * Raised whenever a mission moves to another lifecycle status.
 */
public record MissionStatusChangedEvent(
        Long missionId,
        Long parcelId,
        MissionStatus previousStatus,
        MissionStatus status) {
}
