package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that authorizes a fully planned mission.
 */
public record AuthorizeMissionCommand(Long missionId) {
}
