package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests the fumigation areas of a parcel.
 *
 * @param parcelId   identifier of the parcel
 * @param pagination requested slice, or {@link Pagination#unpaged()} for every area
 * @param sortOrder  requested ordering, or {@code null} for the endpoint default
 */
public record GetFumigationAreasByParcelQuery(
        Long parcelId,
        Pagination pagination,
        SortOrder sortOrder) {

    public GetFumigationAreasByParcelQuery {
        if (pagination == null) {
            throw new IllegalArgumentException("pagination must not be null");
        }
    }

    /**
     * Creates an unpaged query with the default ordering.
     */
    public GetFumigationAreasByParcelQuery(Long parcelId) {
        this(parcelId, Pagination.unpaged(), null);
    }
}
