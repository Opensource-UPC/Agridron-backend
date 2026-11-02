package pe.com.agridron.field.domain.model.commands;

import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;

/**
 * Command that delimits a fumigation area inside a parcel.
 *
 * @param parcelId identifier of the owning parcel
 * @param geometry validated WGS 84 boundary of the area
 */
public record DelimitFumigationAreaCommand(Long parcelId, FieldGeometry geometry) {
}
