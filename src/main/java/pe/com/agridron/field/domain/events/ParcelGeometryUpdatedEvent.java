package pe.com.agridron.field.domain.events;

import pe.com.agridron.field.domain.model.aggregates.Parcel;

import java.math.BigDecimal;

/**
 * Raised when the boundary of a parcel changes and its area is recalculated.
 */
public record ParcelGeometryUpdatedEvent(Long parcelId, Long farmId, BigDecimal areaHectares) {

    public static ParcelGeometryUpdatedEvent from(Parcel parcel) {
        return new ParcelGeometryUpdatedEvent(
                parcel.getId(),
                parcel.getFarmId(),
                parcel.getGeometry().areaHectares());
    }
}
