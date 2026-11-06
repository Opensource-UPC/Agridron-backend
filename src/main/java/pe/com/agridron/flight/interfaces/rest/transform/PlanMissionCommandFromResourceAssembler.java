package pe.com.agridron.flight.interfaces.rest.transform;

import pe.com.agridron.flight.domain.model.commands.PlanMissionCommand;
import pe.com.agridron.flight.interfaces.rest.resources.PlanMissionResource;

/**
 * Assembler that converts a {@link PlanMissionResource} into a {@link PlanMissionCommand}.
 */
public final class PlanMissionCommandFromResourceAssembler {

    private PlanMissionCommandFromResourceAssembler() {
    }

    public static PlanMissionCommand toCommandFromResource(PlanMissionResource resource) {
        return new PlanMissionCommand(resource.parcelId(), resource.requestedDate());
    }
}
