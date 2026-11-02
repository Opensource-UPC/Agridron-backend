package pe.com.agridron.field.domain.events;

/**
 * Raised when a supervisor is removed from a farm.
 */
public record FarmSupervisorRemovedEvent(Long farmId, Long supervisorId) {
}
