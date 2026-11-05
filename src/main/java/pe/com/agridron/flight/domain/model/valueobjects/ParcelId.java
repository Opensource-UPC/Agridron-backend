package pe.com.agridron.flight.domain.model.valueobjects;

/**
 * Value object that references a parcel of the Field Management bounded context by identity.
 *
 * <p>Only the id is stored; there is no physical foreign key across bounded contexts.</p>
 *
 * @param parcelId the identifier, never null and greater than zero
 */
public record ParcelId(Long parcelId) {

    public ParcelId {
        if (parcelId == null || parcelId < 1) {
            throw new IllegalArgumentException("parcelId must be positive");
        }
    }
}
