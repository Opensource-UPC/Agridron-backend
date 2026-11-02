package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests a parcel by its identifier.
 *
 * @param parcelId identifier of the parcel
 */
public record GetParcelByIdQuery(Long parcelId) {
}
