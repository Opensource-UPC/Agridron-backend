package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.commands.GenerateMissionReportCommand;
import pe.com.agridron.analytics.interfaces.rest.resources.CreateMissionReportResource;

import java.time.Instant;

public final class GenerateMissionReportCommandFromResourceAssembler {

    private GenerateMissionReportCommandFromResourceAssembler() {
    }

    public static GenerateMissionReportCommand toCommandFromResource(
            CreateMissionReportResource resource, Instant generatedAt) {
        return new GenerateMissionReportCommand(
                resource.missionId(),
                resource.parcelId(),
                resource.type(),
                resource.treatedArea(),
                resource.appliedVolume(),
                resource.observations(),
                generatedAt);
    }
}
