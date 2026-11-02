package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.commands.CreateParcelCommand;
import pe.com.agridron.field.interfaces.rest.resources.CreateParcelResource;

/**
 * Assembler that converts a {@link CreateParcelResource} into a {@link CreateParcelCommand}.
 */
public final class CreateParcelCommandFromResourceAssembler {

    private CreateParcelCommandFromResourceAssembler() {
    }

    /**
     * Converts the request body into the domain command, parsing the GeoJSON boundary.
     *
     * @param resource the validated request body
     * @return the creation command
     */
    public static CreateParcelCommand toCommandFromResource(CreateParcelResource resource) {
        return new CreateParcelCommand(
                resource.farmId(),
                resource.name(),
                FieldGeometryFromPolygonResourceAssembler
                        .toFieldGeometryFromResource(resource.geometry()),
                resource.cropId(),
                resource.phenologicalStage());
    }
}
