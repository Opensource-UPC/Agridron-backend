package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that completes a mission in progress and registers its result.
 */
public record CompleteMissionCommand(Long missionId, Double treatedArea, Double appliedVolume, String observations) {
}
