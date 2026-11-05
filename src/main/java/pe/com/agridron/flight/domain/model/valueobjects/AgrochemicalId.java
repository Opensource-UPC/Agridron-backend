package pe.com.agridron.flight.domain.model.valueobjects;

/**
 * Value object that references an agrochemical supply of the Inventory bounded context by identity.
 *
 * <p>Only the id is stored; there is no physical foreign key across bounded contexts.</p>
 *
 * @param agrochemicalId the identifier, never null and greater than zero
 */
public record AgrochemicalId(Long agrochemicalId) {

    public AgrochemicalId {
        if (agrochemicalId == null || agrochemicalId < 1) {
            throw new IllegalArgumentException("agrochemicalId must be positive");
        }
    }
}
