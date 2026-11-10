package pe.com.agridron.field.domain.model.commands;

/**
 * Command that removes a supervisor from a farm.
 *
 * @param farmId       identifier of the farm
 * @param supervisorId identifier of the supervisor in the IAM bounded context
 */
public record RemoveFarmSupervisorCommand(Long farmId, Long supervisorId) {
}
