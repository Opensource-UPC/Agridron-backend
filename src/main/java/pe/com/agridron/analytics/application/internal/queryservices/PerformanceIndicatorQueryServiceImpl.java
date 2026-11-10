package pe.com.agridron.analytics.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.analytics.application.queryservices.PerformanceIndicatorQueryService;
import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.domain.model.queries.GetPerformanceIndicatorsByPeriodQuery;
import pe.com.agridron.analytics.domain.repositories.PerformanceIndicatorRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

@Service
public class PerformanceIndicatorQueryServiceImpl implements PerformanceIndicatorQueryService {

    private final PerformanceIndicatorRepository performanceIndicatorRepository;

    public PerformanceIndicatorQueryServiceImpl(PerformanceIndicatorRepository performanceIndicatorRepository) {
        this.performanceIndicatorRepository = performanceIndicatorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<PerformanceIndicator>, ApplicationError> handle(GetPerformanceIndicatorsByPeriodQuery query) {
        if (query.period() == null) {
            return Result.success(performanceIndicatorRepository.findAll());
        }
        try {
            var period = PerformanceIndicator.normalizePeriod(query.period());
            return Result.success(performanceIndicatorRepository.findByPeriod(period));
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("period", ex.getMessage()));
        }
    }
}
