package pe.com.agridron.flight.domain.model.valueobjects;

/**
 * Value object that references the drone operator, a user of the IAM bounded context by identity.
 *
 * <p>Only the id is stored; there is no physical foreign key across bounded contexts.</p>
 *
 * @param operatorId the identifier, never null and greater than zero
 */
public record OperatorId(Long operatorId) {

    public OperatorId {
        if (operatorId == null || operatorId < 1) {
            throw new IllegalArgumentException("operatorId must be positive");
        }
    }
}
