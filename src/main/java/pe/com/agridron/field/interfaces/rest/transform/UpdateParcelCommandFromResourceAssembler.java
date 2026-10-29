package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.commands.UpdateParcelCommand;
import pe.com.agridron.field.interfaces.rest.resources.UpdateParcelResource;

/**
 * Assembler that converts an {@link UpdateParcelResource} into an {@link UpdateParcelCommand}.
 */
public final class UpdateParcelCommandFromResourceAssembler {

    private UpdateParcelCommandFromResourceAssembler() {
    }

    /**
     * Converts the request body and the path variable into the domain command.
     *
     * @param parcelId identifier taken from the path
     * @param resource the validated request body
     * @return the update command
     */
    public static UpdateParcelCommand toCommandFromResource(Long parcelId, UpdateParcelResource resource) {
        return new UpdateParcelCommand(
                parcelId,
                resource.name(),
                resource.cropId(),
                resource.phenologicalStage());
    }
}
