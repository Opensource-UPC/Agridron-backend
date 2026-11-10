package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;

import java.time.LocalDate;
import java.util.List;

public final class MissionHistorySearchCriteriaFromRequestAssembler {

    private MissionHistorySearchCriteriaFromRequestAssembler() {
    }

    public static MissionHistorySearchCriteria toCriteria(
            Long missionId, List<Long> parcelIds, MissionFinalStatus status, LocalDate from, LocalDate to) {
        return new MissionHistorySearchCriteria(
                missionId == null ? null : List.of(missionId),
                parcelIds,
                status,
                SearchPeriodAssembler.toFromInstant(from),
                SearchPeriodAssembler.toToInstant(to));
    }
}
