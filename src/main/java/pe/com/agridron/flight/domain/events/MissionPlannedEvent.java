package pe.com.agridron.flight.domain.events;

import pe.com.agridron.flight.domain.model.aggregates.Mission;

import java.time.LocalDate;

/**
 * Raised when a mission has been persisted and its identity is available.
 */
public record MissionPlannedEvent(Long missionId, Long parcelId, LocalDate requestedDate) {

    public static MissionPlannedEvent from(Mission mission) {
        return new MissionPlannedEvent(
                mission.getId(),
                mission.getParcelId().parcelId(),
                mission.getRequestedDate());
    }
}
