package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that pauses a mission in progress.
 */
public record PauseMissionCommand(Long missionId) {
}
