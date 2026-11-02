package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.commands.UpdateFarmCommand;
import pe.com.agridron.field.interfaces.rest.resources.UpdateFarmResource;

/**
 * Assembler that converts an {@link UpdateFarmResource} into an {@link UpdateFarmCommand}.
 */
public final class UpdateFarmCommandFromResourceAssembler {

    private UpdateFarmCommandFromResourceAssembler() {
    }

    /**
     * Converts the request body and the path variable into the domain command.
     *
     * @param farmId   identifier taken from the path
     * @param resource the validated request body
     * @return the update command
     */
    public static UpdateFarmCommand toCommandFromResource(Long farmId, UpdateFarmResource resource) {
        return new UpdateFarmCommand(
                farmId,
                resource.name(),
                resource.location(),
                resource.department());
    }
}
