package pe.com.agridron.field.domain.events;

import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;

/**
 * Raised when the crop and phenological stage of a parcel are registered.
 */
public record ParcelCropInformationRegisteredEvent(
        Long parcelId,
        Long cropId,
        PhenologicalStage phenologicalStage) {

    public static ParcelCropInformationRegisteredEvent from(Parcel parcel) {
        return new ParcelCropInformationRegisteredEvent(
                parcel.getId(),
                parcel.getCropId(),
                parcel.getPhenologicalStage());
    }
}
