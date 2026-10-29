package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.commands.CreateFarmCommand;
import pe.com.agridron.field.interfaces.rest.resources.CreateFarmResource;

/**
 * Assembler that converts a {@link CreateFarmResource} into a {@link CreateFarmCommand}.
 */
public final class CreateFarmCommandFromResourceAssembler {

    private CreateFarmCommandFromResourceAssembler() {
    }

    /**
     * Converts the request body into the domain command.
     *
     * @param resource the validated request body
     * @return the creation command
     */
    public static CreateFarmCommand toCommandFromResource(CreateFarmResource resource) {
        return new CreateFarmCommand(
                resource.ownerId(),
                resource.name(),
                resource.location(),
                resource.department());
    }
}
