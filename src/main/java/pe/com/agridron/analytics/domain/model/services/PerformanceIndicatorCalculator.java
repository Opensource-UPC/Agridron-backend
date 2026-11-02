package pe.com.agridron.analytics.domain.model.services;

import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.valueobjects.IndicatorMetric;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates the performance indicators of a set of archived missions and their reports.
 * A mission can have several reports that repeat the same area and volume, so each completed
 * mission is counted only once (preferring its mission summary report).
 */
public final class PerformanceIndicatorCalculator {

    /**
     * Value calculated for one indicator.
     */
    public record IndicatorValue(IndicatorMetric metric, BigDecimal value) {
    }

    private static final int SCALE = 2;

    private PerformanceIndicatorCalculator() {
    }

    public static List<IndicatorValue> calculate(
            Collection<MissionHistory> histories, Collection<MissionReport> reports) {
        Set<Long> completedMissionIds = histories.stream()
                .filter(history -> history.getFinalStatus() == MissionFinalStatus.COMPLETED)
                .map(MissionHistory::getMissionId)
                .collect(Collectors.toSet());

        Map<Long, MissionReport> reportByMission = new HashMap<>();
        reports.stream()
                .filter(report -> completedMissionIds.contains(report.getMissionId()))
                .sorted(Comparator
                        .comparing((MissionReport report) -> report.getType() != ReportType.MISSION_SUMMARY)
                        .thenComparing(MissionReport::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(report -> reportByMission.putIfAbsent(report.getMissionId(), report));

        var hectares = reportByMission.values().stream()
                .map(MissionReport::getTreatedArea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var volume = reportByMission.values().stream()
                .map(MissionReport::getAppliedVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var completionRate = histories.isEmpty()
                ? BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(completedMissionIds.size())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(histories.size()), SCALE, RoundingMode.HALF_UP);
        var applicationRate = hectares.signum() == 0
                ? BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)
                : volume.divide(hectares, SCALE, RoundingMode.HALF_UP);

        List<IndicatorValue> values = new ArrayList<>();
        values.add(new IndicatorValue(IndicatorMetric.HECTARES_TREATED, hectares.setScale(SCALE, RoundingMode.HALF_UP)));
        values.add(new IndicatorValue(IndicatorMetric.COMPLETION_RATE, completionRate));
        values.add(new IndicatorValue(IndicatorMetric.APPLIED_VOLUME, volume.setScale(SCALE, RoundingMode.HALF_UP)));
        values.add(new IndicatorValue(IndicatorMetric.APPLICATION_RATE, applicationRate));
        return values;
    }
}
