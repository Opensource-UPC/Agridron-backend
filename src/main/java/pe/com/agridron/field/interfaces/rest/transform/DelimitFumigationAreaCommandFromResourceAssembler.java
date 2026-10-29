package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.commands.DelimitFumigationAreaCommand;
import pe.com.agridron.field.interfaces.rest.resources.CreateFumigationAreaResource;

/**
 * Assembler that converts a {@link CreateFumigationAreaResource} into a
 * {@link DelimitFumigationAreaCommand}.
 */
public final class DelimitFumigationAreaCommandFromResourceAssembler {

    private DelimitFumigationAreaCommandFromResourceAssembler() {
    }

    /**
     * Converts the request body into the domain command, parsing the GeoJSON boundary.
     *
     * @param resource the validated request body
     * @return the delimitation command
     */
    public static DelimitFumigationAreaCommand toCommandFromResource(
            CreateFumigationAreaResource resource) {
        return new DelimitFumigationAreaCommand(
                resource.parcelId(),
                FieldGeometryFromPolygonResourceAssembler
                        .toFieldGeometryFromResource(resource.geometry()));
    }
}
