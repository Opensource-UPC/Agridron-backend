package pe.com.agridron.flight.domain.model.queries;

/**
 * Query that requests a mission by its identifier.
 */
public record GetMissionByIdQuery(Long missionId) {
}
