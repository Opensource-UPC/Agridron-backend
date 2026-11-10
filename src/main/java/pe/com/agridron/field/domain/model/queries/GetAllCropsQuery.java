package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests the crops of the read-only catalog.
 *
 * @param pagination requested slice, or {@link Pagination#unpaged()} for the whole catalog
 * @param sortOrder  requested ordering, or {@code null} for the endpoint default
 */
public record GetAllCropsQuery(Pagination pagination, SortOrder sortOrder) {

    public GetAllCropsQuery {
        if (pagination == null) {
            throw new IllegalArgumentException("pagination must not be null");
        }
    }

    /**
     * Creates an unpaged query with the default ordering.
     */
    public GetAllCropsQuery() {
        this(Pagination.unpaged(), null);
    }
}
