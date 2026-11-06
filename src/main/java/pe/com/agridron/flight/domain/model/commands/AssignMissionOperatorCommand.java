package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that assigns the drone operator of a planned mission.
 */
public record AssignMissionOperatorCommand(Long missionId, Long operatorId) {
}
