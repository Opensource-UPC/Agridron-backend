package pe.com.agridron.flight.interfaces.rest.transform;

import pe.com.agridron.flight.domain.model.commands.CompleteMissionCommand;
import pe.com.agridron.flight.interfaces.rest.resources.CompleteMissionResource;

/**
 * Assembler that converts a {@link CompleteMissionResource} into a {@link CompleteMissionCommand}.
 */
public final class CompleteMissionCommandFromResourceAssembler {

    private CompleteMissionCommandFromResourceAssembler() {
    }

    public static CompleteMissionCommand toCommandFromResource(Long missionId, CompleteMissionResource resource) {
        return new CompleteMissionCommand(
                missionId,
                resource.treatedArea(),
                resource.appliedVolume(),
                resource.observations());
    }
}
