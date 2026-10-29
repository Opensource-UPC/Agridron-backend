package pe.com.agridron.field.domain.model.queries;

import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;

import java.math.BigDecimal;

/**
 * Optional filters used to search parcels.
 *
 * @param farmId           farm to filter by, or {@code null} for all farms
 * @param status           operational status to filter by, or {@code null}
 * @param cropId           crop to filter by, or {@code null}
 * @param minAreaHectares  inclusive minimum area, or {@code null}
 * @param maxAreaHectares  inclusive maximum area, or {@code null}
 */
public record ParcelSearchCriteria(
        Long farmId,
        ParcelStatus status,
        Long cropId,
        BigDecimal minAreaHectares,
        BigDecimal maxAreaHectares) {

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
    }
}
