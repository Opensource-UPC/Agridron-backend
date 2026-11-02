package pe.com.agridron.analytics.application.queryservices;

import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.domain.model.queries.GetPerformanceIndicatorsByPeriodQuery;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

public interface PerformanceIndicatorQueryService {

    Result<List<PerformanceIndicator>, ApplicationError> handle(GetPerformanceIndicatorsByPeriodQuery query);
}
