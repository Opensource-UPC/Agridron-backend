package pe.com.agridron.flight.domain.model.queries;

/**
 * Query that requests the incidents reported during a mission.
 */
public record GetIncidentsByMissionIdQuery(Long missionId) {
}
