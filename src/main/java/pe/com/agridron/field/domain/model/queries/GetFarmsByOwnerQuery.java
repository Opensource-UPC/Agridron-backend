package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests the farms owned by a user.
 *
 * @param ownerId    identifier of the owner in the IAM bounded context
 * @param pagination requested slice, or {@link Pagination#unpaged()} for every farm
 * @param sortOrder  requested ordering, or {@code null} for the endpoint default
 */
public record GetFarmsByOwnerQuery(
        Long ownerId,
        Pagination pagination,
        SortOrder sortOrder) {

    public GetFarmsByOwnerQuery {
        if (pagination == null) {
            throw new IllegalArgumentException("pagination must not be null");
        }
    }

    /**
     * Creates an unpaged query with the default ordering.
     */
    public GetFarmsByOwnerQuery(Long ownerId) {
        this(ownerId, Pagination.unpaged(), null);
    }
}
