package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that cancels a mission that has not finished.
 */
public record CancelMissionCommand(Long missionId) {
}
