package pe.com.agridron.field.domain.events;

import pe.com.agridron.field.domain.model.aggregates.Parcel;

import java.math.BigDecimal;

/**
 * Raised when a parcel has been persisted and its identity is available.
 */
public record ParcelRegisteredEvent(Long parcelId, Long farmId, BigDecimal areaHectares) {

    public static ParcelRegisteredEvent from(Parcel parcel) {
        return new ParcelRegisteredEvent(
                parcel.getId(),
                parcel.getFarmId(),
                parcel.getGeometry().areaHectares());
    }
}
