package pe.com.agridron.flight.domain.model.valueobjects;

/**
 * Value object that references a drone of the Inventory bounded context by identity.
 *
 * <p>Only the id is stored; there is no physical foreign key across bounded contexts.</p>
 *
 * @param droneId the identifier, never null and greater than zero
 */
public record DroneId(Long droneId) {

    public DroneId {
        if (droneId == null || droneId < 1) {
            throw new IllegalArgumentException("droneId must be positive");
        }
    }
}
