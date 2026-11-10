package pe.com.agridron.analytics.domain.model.services;

import org.junit.jupiter.api.Test;
import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.services.PerformanceIndicatorCalculator.IndicatorValue;
import pe.com.agridron.analytics.domain.model.valueobjects.IndicatorMetric;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceIndicatorCalculatorTest {

    private static final Instant AT = Instant.parse("2026-09-20T15:00:00Z");

    private static MissionHistory history(long id, long missionId, MissionFinalStatus status) {
        return MissionHistory.reconstitute(id, missionId, 1L, status, AT);
    }

    private static MissionReport report(long id, long missionId, ReportType type, String area, String volume) {
        return MissionReport.reconstitute(
                id, missionId, 1L, type, new BigDecimal(area), new BigDecimal(volume), "", AT, null);
    }

    private static BigDecimal valueOf(List<IndicatorValue> values, IndicatorMetric metric) {
        return values.stream()
                .filter(value -> value.metric() == metric)
                .findFirst()
                .orElseThrow()
                .value();
    }

    @Test
    void countsEachCompletedMissionOnlyOnce() {
        var histories = List.of(
                history(1, 1, MissionFinalStatus.COMPLETED),
                history(2, 2, MissionFinalStatus.COMPLETED),
                history(3, 3, MissionFinalStatus.CANCELLED));
        var reports = List.of(
                report(1, 1, ReportType.MISSION_SUMMARY, "12.50", "38.00"),
                report(2, 1, ReportType.PHYTOSANITARY, "12.50", "38.00"),
                report(3, 2, ReportType.MISSION_SUMMARY, "8.20", "24.60"),
                report(4, 3, ReportType.MISSION_SUMMARY, "5.00", "15.00"));

        var values = PerformanceIndicatorCalculator.calculate(histories, reports);

        assertThat(valueOf(values, IndicatorMetric.HECTARES_TREATED)).isEqualByComparingTo("20.70");
        assertThat(valueOf(values, IndicatorMetric.APPLIED_VOLUME)).isEqualByComparingTo("62.60");
        assertThat(valueOf(values, IndicatorMetric.COMPLETION_RATE)).isEqualByComparingTo("66.67");
        assertThat(valueOf(values, IndicatorMetric.APPLICATION_RATE)).isEqualByComparingTo("3.02");
    }

    @Test
    void returnsZeroesWhenThereIsNoData() {
        var values = PerformanceIndicatorCalculator.calculate(List.of(), List.of());

        assertThat(values).hasSize(IndicatorMetric.values().length);
        assertThat(values).allSatisfy(value -> assertThat(value.value()).isEqualByComparingTo("0"));
    }

    @Test
    void usesPhytosanitaryReportWhenThereIsNoSummary() {
        var histories = List.of(history(1, 1, MissionFinalStatus.COMPLETED));
        var reports = List.of(report(1, 1, ReportType.PHYTOSANITARY, "10.00", "30.00"));

        var values = PerformanceIndicatorCalculator.calculate(histories, reports);

        assertThat(valueOf(values, IndicatorMetric.HECTARES_TREATED)).isEqualByComparingTo("10.00");
        assertThat(valueOf(values, IndicatorMetric.APPLICATION_RATE)).isEqualByComparingTo("3.00");
    }
}
