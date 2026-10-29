package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.interfaces.rest.resources.CropResource;

/**
 * Assembler that converts a {@link Crop} aggregate into a {@link CropResource}.
 */
public final class CropResourceFromEntityAssembler {

    private CropResourceFromEntityAssembler() {
    }

    /**
     * Converts the catalog entry into its REST representation.
     *
     * @param crop the crop aggregate
     * @return the response resource
     */
    public static CropResource toResourceFromEntity(Crop crop) {
        return new CropResource(crop.getId(), crop.getName(), crop.getVariety());
    }
}
