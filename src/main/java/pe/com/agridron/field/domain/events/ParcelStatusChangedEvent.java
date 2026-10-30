package pe.com.agridron.field.domain.events;

import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;

/**
 * Raised when a parcel changes its operational status.
 */
public record ParcelStatusChangedEvent(Long parcelId, Long farmId, ParcelStatus status) {
}
