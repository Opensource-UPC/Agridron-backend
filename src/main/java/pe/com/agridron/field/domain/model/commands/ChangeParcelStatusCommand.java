package pe.com.agridron.field.domain.model.commands;

import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;

/**
 * Command that changes the operational status of a parcel.
 *
 * @param parcelId identifier of the parcel
 * @param status   the target status
 */
public record ChangeParcelStatusCommand(Long parcelId, ParcelStatus status) {
}
