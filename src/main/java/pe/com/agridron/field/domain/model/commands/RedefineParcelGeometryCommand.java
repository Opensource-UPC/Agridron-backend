package pe.com.agridron.field.domain.model.commands;

import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;

/**
 * Command that redefines the boundary of a parcel.
 *
 * @param parcelId identifier of the parcel
 * @param geometry the new validated boundary
 */
public record RedefineParcelGeometryCommand(Long parcelId, FieldGeometry geometry) {
}
