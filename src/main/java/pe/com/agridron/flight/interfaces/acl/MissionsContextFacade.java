package pe.com.agridron.flight.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.com.agridron.flight.application.queryservices.MissionQueryService;
import pe.com.agridron.flight.domain.model.queries.ExistsActiveMissionByParcelIdQuery;

/**
 * Published synchronous contract of Flight Operations for other bounded contexts.
 *
 * <p>It exposes only primitive types, so consumers never depend on the Flight domain model.
 * Field Management uses it to enforce FM-006 (ADR-010).</p>
 */
@Service
public class MissionsContextFacade {

    private final MissionQueryService missionQueryService;

    public MissionsContextFacade(MissionQueryService missionQueryService) {
        this.missionQueryService = missionQueryService;
    }

    /**
     * Checks whether a parcel takes part in a mission that is not completed or cancelled.
     *
     * @param parcelId identifier of the parcel
     * @return {@code true} when the parcel is used by at least one active mission
     */
    public boolean hasActiveMissionsForParcel(Long parcelId) {
        if (parcelId == null || parcelId < 1) {
            return false;
        }
        return missionQueryService.handle(new ExistsActiveMissionByParcelIdQuery(parcelId));
    }
}
