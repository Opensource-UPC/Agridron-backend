package pe.com.agridron.analytics.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.analytics.application.commandservices.PerformanceIndicatorCommandService;
import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.domain.model.commands.CalculatePerformanceIndicatorsCommand;
import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.model.services.PerformanceIndicatorCalculator;
import pe.com.agridron.analytics.domain.repositories.MissionHistoryRepository;
import pe.com.agridron.analytics.domain.repositories.MissionReportRepository;
import pe.com.agridron.analytics.domain.repositories.PerformanceIndicatorRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class PerformanceIndicatorCommandServiceImpl implements PerformanceIndicatorCommandService {

    private final MissionHistoryRepository missionHistoryRepository;
    private final MissionReportRepository missionReportRepository;
    private final PerformanceIndicatorRepository performanceIndicatorRepository;

    public PerformanceIndicatorCommandServiceImpl(
            MissionHistoryRepository missionHistoryRepository,
            MissionReportRepository missionReportRepository,
            PerformanceIndicatorRepository performanceIndicatorRepository) {
        this.missionHistoryRepository = missionHistoryRepository;
        this.missionReportRepository = missionReportRepository;
        this.performanceIndicatorRepository = performanceIndicatorRepository;
    }

    @Override
    @Transactional
    public Result<List<PerformanceIndicator>, ApplicationError> handle(CalculatePerformanceIndicatorsCommand command) {
        String period;
        Instant from;
        Instant to;
        try {
            period = PerformanceIndicator.normalizePeriod(command.period());
            var month = YearMonth.parse(period);
            from = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            to = month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("period", ex.getMessage()));
        }
        if (command.calculatedAt() == null) {
            return Result.failure(ApplicationError.validationError("calculatedAt", "calculatedAt is required"));
        }

        var histories = missionHistoryRepository.search(
                new MissionHistorySearchCriteria(null, null, null, from, to));
        if (histories.isEmpty()) {
            return Result.success(List.of());
        }
        var missionIds = histories.stream().map(history -> history.getMissionId()).toList();
        var reports = missionReportRepository.search(
                new MissionReportSearchCriteria(missionIds, null, null, null, null));

        List<PerformanceIndicator> saved = new ArrayList<>();
        for (var calculated : PerformanceIndicatorCalculator.calculate(histories, reports)) {
            var name = calculated.metric().displayName();
            var indicator = performanceIndicatorRepository.findByNameAndPeriod(name, period)
                    .map(existing -> {
                        existing.recalculate(calculated.value(), command.calculatedAt());
                        return existing;
                    })
                    .orElseGet(() -> PerformanceIndicator.calculate(
                            name,
                            calculated.value(),
                            calculated.metric().unit(),
                            period,
                            command.calculatedAt()));
            saved.add(performanceIndicatorRepository.save(indicator));
        }
        return Result.success(List.copyOf(saved));
    }
}
