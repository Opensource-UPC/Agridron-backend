package pe.com.agridron.flight.domain.model.queries;

/**
 * Query that checks whether a parcel takes part in a mission that has not finished.
 */
public record ExistsActiveMissionByParcelIdQuery(Long parcelId) {
}
