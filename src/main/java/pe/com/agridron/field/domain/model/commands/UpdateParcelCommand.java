package pe.com.agridron.field.domain.model.commands;

import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;

/**
 * Command that updates the editable information of a parcel.
 *
 * <p>The boundary is changed through {@link RedefineParcelGeometryCommand}.</p>
 *
 * @param parcelId          identifier of the parcel
 * @param name              new parcel name, unique inside its farm
 * @param cropId            optional crop from the read-only catalog
 * @param phenologicalStage required when {@code cropId} is present
 */
public record UpdateParcelCommand(
        Long parcelId,
        String name,
        Long cropId,
        PhenologicalStage phenologicalStage) {
}
