package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that assigns the drone and agrochemical of a planned mission.
 */
public record AssignMissionResourcesCommand(Long missionId, Long droneId, Long agrochemicalId) {
}
