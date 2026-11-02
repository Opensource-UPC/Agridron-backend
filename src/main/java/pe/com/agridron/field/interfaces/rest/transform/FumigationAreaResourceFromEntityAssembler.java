package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.interfaces.rest.resources.FumigationAreaResource;

/**
 * Assembler that converts a {@link FumigationArea} aggregate into a
 * {@link FumigationAreaResource}.
 */
public final class FumigationAreaResourceFromEntityAssembler {

    private FumigationAreaResourceFromEntityAssembler() {
    }

    /**
     * Converts the aggregate into its REST representation.
     *
     * @param area the fumigation area aggregate
     * @return the response resource
     */
    public static FumigationAreaResource toResourceFromEntity(FumigationArea area) {
        return new FumigationAreaResource(
                area.getId(),
                area.getParcelId(),
                PolygonResourceFromFieldGeometryAssembler
                        .toResourceFromFieldGeometry(area.getGeometry()),
                area.getGeometry().areaHectares());
    }
}
