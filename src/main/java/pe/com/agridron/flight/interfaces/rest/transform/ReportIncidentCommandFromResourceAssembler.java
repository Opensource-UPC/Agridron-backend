package pe.com.agridron.flight.interfaces.rest.transform;

import pe.com.agridron.flight.domain.model.commands.ReportIncidentCommand;
import pe.com.agridron.flight.interfaces.rest.resources.ReportIncidentResource;

/**
 * Assembler that converts a {@link ReportIncidentResource} into a {@link ReportIncidentCommand}.
 */
public final class ReportIncidentCommandFromResourceAssembler {

    private ReportIncidentCommandFromResourceAssembler() {
    }

    public static ReportIncidentCommand toCommandFromResource(Long missionId, ReportIncidentResource resource) {
        return new ReportIncidentCommand(
                missionId,
                resource.type(),
                resource.description(),
                resource.occurredAt());
    }
}
