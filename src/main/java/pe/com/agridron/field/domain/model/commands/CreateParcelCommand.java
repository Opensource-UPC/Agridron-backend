package pe.com.agridron.field.domain.model.commands;

import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;

/**
 * Command that requests the creation of a parcel inside a farm.
 *
 * @param farmId            identifier of the owning farm
 * @param name              parcel name, unique inside the farm
 * @param geometry          validated WGS 84 boundary
 * @param cropId            optional crop from the read-only catalog
 * @param phenologicalStage required when {@code cropId} is present
 */
public record CreateParcelCommand(
        Long farmId,
        String name,
        FieldGeometry geometry,
        Long cropId,
        PhenologicalStage phenologicalStage) {
}
