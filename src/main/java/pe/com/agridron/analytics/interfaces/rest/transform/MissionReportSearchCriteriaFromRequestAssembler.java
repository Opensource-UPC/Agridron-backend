package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;

import java.time.LocalDate;
import java.util.List;

public final class MissionReportSearchCriteriaFromRequestAssembler {

    private MissionReportSearchCriteriaFromRequestAssembler() {
    }

    public static MissionReportSearchCriteria toCriteria(
            Long missionId, List<Long> parcelIds, ReportType type, LocalDate from, LocalDate to) {
        return new MissionReportSearchCriteria(
                missionId == null ? null : List.of(missionId),
                parcelIds,
                type,
                SearchPeriodAssembler.toFromInstant(from),
                SearchPeriodAssembler.toToInstant(to));
    }
}
