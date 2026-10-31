package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.commands.RegisterMissionHistoryCommand;
import pe.com.agridron.analytics.interfaces.rest.resources.RegisterMissionHistoryResource;

public final class RegisterMissionHistoryCommandFromResourceAssembler {

    private RegisterMissionHistoryCommandFromResourceAssembler() {
    }

    public static RegisterMissionHistoryCommand toCommandFromResource(RegisterMissionHistoryResource resource) {
        return new RegisterMissionHistoryCommand(
                resource.missionId(), resource.parcelId(), resource.finalStatus(), resource.completedAt());
    }
}
