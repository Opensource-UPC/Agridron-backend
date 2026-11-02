package pe.com.agridron.field.domain.model.commands;

/**
 * Command that assigns a supervisor to a farm.
 *
 * @param farmId       identifier of the farm
 * @param supervisorId identifier of the supervisor in the IAM bounded context
 */
public record AssignFarmSupervisorCommand(Long farmId, Long supervisorId) {
}
