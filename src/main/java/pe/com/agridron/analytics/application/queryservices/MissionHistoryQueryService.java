package pe.com.agridron.analytics.application.queryservices;

import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.queries.GetMissionHistoryByIdQuery;
import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

public interface MissionHistoryQueryService {

    Result<MissionHistory, ApplicationError> handle(GetMissionHistoryByIdQuery query);

    List<MissionHistory> handle(MissionHistorySearchCriteria criteria);
}
