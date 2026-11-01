package pe.com.agridron.field.domain.model.queries;

import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;

import java.math.BigDecimal;

/**
 * Optional filters used to search parcels, plus the requested slice and ordering.
 *
 * @param farmId           farm to filter by, or {@code null} for all farms
 * @param status           operational status to filter by, or {@code null}
 * @param cropId           crop to filter by, or {@code null}
 * @param minAreaHectares  inclusive minimum area, or {@code null}
 * @param maxAreaHectares  inclusive maximum area, or {@code null}
 * @param pagination       requested slice, or {@link Pagination#unpaged()}
 * @param sortOrder        requested ordering, or {@code null} for the endpoint default
 */
public record ParcelSearchCriteria(
        Long farmId,
        ParcelStatus status,
        Long cropId,
        BigDecimal minAreaHectares,
        BigDecimal maxAreaHectares,
        Pagination pagination,
        SortOrder sortOrder) {

    public ParcelSearchCriteria {
        if (farmId != null && farmId <= 0) {
            throw new IllegalArgumentException("farmId must be positive");
        }
        if (cropId != null && cropId <= 0) {
            throw new IllegalArgumentException("cropId must be positive");
        }
        if (minAreaHectares != null && minAreaHectares.signum() < 0) {
            throw new IllegalArgumentException("minAreaHectares must not be negative");
        }
        if (maxAreaHectares != null && maxAreaHectares.signum() < 0) {
            throw new IllegalArgumentException("maxAreaHectares must not be negative");
        }
        if (minAreaHectares != null && maxAreaHectares != null
                && minAreaHectares.compareTo(maxAreaHectares) > 0) {
            throw new IllegalArgumentException("minAreaHectares must not be greater than maxAreaHectares");
        }
        if (pagination == null) {
            throw new IllegalArgumentException("pagination must not be null");
        }
    }

    /**
     * Creates an unpaged search with the default ordering.
     */
    public ParcelSearchCriteria(
            Long farmId,
            ParcelStatus status,
            Long cropId,
            BigDecimal minAreaHectares,
            BigDecimal maxAreaHectares) {
        this(farmId, status, cropId, minAreaHectares, maxAreaHectares,
                Pagination.unpaged(), null);
    }
}
