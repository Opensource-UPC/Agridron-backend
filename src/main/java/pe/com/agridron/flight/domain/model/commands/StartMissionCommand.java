package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that starts an authorized mission.
 */
public record StartMissionCommand(Long missionId) {
}
