package pe.com.agridron.flight.interfaces.rest.transform;

import pe.com.agridron.flight.domain.model.commands.UpdateOperationStatusCommand;
import pe.com.agridron.flight.interfaces.rest.resources.UpdateOperationStatusResource;

/**
 * Assembler that converts an {@link UpdateOperationStatusResource} into an {@link UpdateOperationStatusCommand}.
 */
public final class UpdateOperationStatusCommandFromResourceAssembler {

    private UpdateOperationStatusCommandFromResourceAssembler() {
    }

    public static UpdateOperationStatusCommand toCommandFromResource(
            Long missionId, UpdateOperationStatusResource resource) {
        return new UpdateOperationStatusCommand(
                missionId,
                resource.latitude(),
                resource.longitude(),
                resource.state(),
                resource.timestamp());
    }
}
