package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that resumes a paused mission.
 */
public record ResumeMissionCommand(Long missionId) {
}
