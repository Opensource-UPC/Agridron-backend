package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests every fumigation area of a parcel.
 *
 * @param parcelId identifier of the parcel
 */
public record GetFumigationAreasByParcelQuery(Long parcelId) {
}
