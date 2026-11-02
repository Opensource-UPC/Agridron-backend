package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.interfaces.rest.resources.MissionHistoryResource;

public final class MissionHistoryResourceFromEntityAssembler {

    private MissionHistoryResourceFromEntityAssembler() {
    }

    public static MissionHistoryResource toResourceFromEntity(MissionHistory history) {
        return new MissionHistoryResource(
                history.getId(),
                history.getMissionId(),
                history.getParcelId(),
                history.getFinalStatus(),
                history.getCompletedAt());
    }
}
