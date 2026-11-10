package pe.com.agridron.analytics.application.queryservices;

import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.queries.GetMissionReportByIdQuery;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

public interface MissionReportQueryService {

    Result<MissionReport, ApplicationError> handle(GetMissionReportByIdQuery query);

    List<MissionReport> handle(MissionReportSearchCriteria criteria);
}
