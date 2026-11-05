package pe.com.agridron.flight.domain.model.queries;

/**
 * Query that requests the last operational status of a mission.
 */
public record GetOperationStatusByMissionIdQuery(Long missionId) {
}
