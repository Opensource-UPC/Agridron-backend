package pe.com.agridron.flight.domain.events;

import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.entities.Incident;

import java.time.Instant;

/**
 * Raised when an incident is reported during a mission.
 */
public record IncidentReportedEvent(
        Long missionId,
        Long parcelId,
        String type,
        String description,
        Instant occurredAt) {

    public static IncidentReportedEvent from(Mission mission, Incident incident) {
        return new IncidentReportedEvent(
                mission.getId(),
                mission.getParcelId().parcelId(),
                incident.getType(),
                incident.getDescription(),
                incident.getOccurredAt());
    }
}
