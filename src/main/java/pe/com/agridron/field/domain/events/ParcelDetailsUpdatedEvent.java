package pe.com.agridron.field.domain.events;

import pe.com.agridron.field.domain.model.aggregates.Parcel;

/**
 * Raised when the descriptive data of a parcel changes.
 */
public record ParcelDetailsUpdatedEvent(Long parcelId, Long farmId, String name) {

    public static ParcelDetailsUpdatedEvent from(Parcel parcel) {
        return new ParcelDetailsUpdatedEvent(
                parcel.getId(),
                parcel.getFarmId(),
                parcel.getName());
    }
}
