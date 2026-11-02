package pe.com.agridron.field.domain.events;

/**
 * Raised when a supervisor is assigned to a farm.
 */
public record FarmSupervisorAssignedEvent(Long farmId, Long supervisorId) {
}
