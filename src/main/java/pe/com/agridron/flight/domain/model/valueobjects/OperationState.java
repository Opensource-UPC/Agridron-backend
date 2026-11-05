package pe.com.agridron.flight.domain.model.valueobjects;

/**
 * Flight phase reported by the drone while a mission is being executed.
 */
public enum OperationState {
    IDLE,
    TAKEOFF,
    IN_FLIGHT,
    SPRAYING,
    RETURNING,
    LANDED
}
