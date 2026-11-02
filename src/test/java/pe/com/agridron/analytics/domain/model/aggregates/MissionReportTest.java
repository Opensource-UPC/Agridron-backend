package pe.com.agridron.analytics.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.com.agridron.analytics.domain.events.MissionReportGeneratedEvent;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MissionReportTest {

    private static final Instant GENERATED_AT = Instant.parse("2026-09-20T15:30:00Z");

    private static MissionReport report(BigDecimal area, BigDecimal volume) {
        return MissionReport.generate(
                1L, 2L, ReportType.MISSION_SUMMARY, area, volume, "  All good  ", GENERATED_AT);
    }

    @Test
    void generatesReportWithNormalizedObservations() {
        var report = report(new BigDecimal("12.50"), new BigDecimal("38.00"));

        assertThat(report.getId()).isNull();
        assertThat(report.getMissionId()).isEqualTo(1L);
        assertThat(report.getParcelId()).isEqualTo(2L);
        assertThat(report.getType()).isEqualTo(ReportType.MISSION_SUMMARY);
        assertThat(report.getObservations()).isEqualTo("All good");
        assertThat(report.getGeneratedAt()).isEqualTo(GENERATED_AT);
        assertThat(report.getMetrics()).isEmpty();
    }

    @Test
    void treatsMissingObservationsAsEmptyText() {
        var report = MissionReport.generate(
                1L, 2L, ReportType.PHYTOSANITARY, BigDecimal.ONE, BigDecimal.ONE, null, GENERATED_AT);

        assertThat(report.getObservations()).isEmpty();
    }

    @Test
    void rejectsNegativeAreaAndVolume() {
        assertThatThrownBy(() -> report(new BigDecimal("-1"), BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("treatedArea");
        assertThatThrownBy(() -> report(BigDecimal.ONE, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("appliedVolume");
    }

    @Test
    void rejectsMissingReferences() {
        assertThatThrownBy(() -> MissionReport.generate(
                null, 2L, ReportType.MISSION_SUMMARY, BigDecimal.ONE, BigDecimal.ONE, "", GENERATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missionId");
        assertThatThrownBy(() -> MissionReport.generate(
                1L, 0L, ReportType.MISSION_SUMMARY, BigDecimal.ONE, BigDecimal.ONE, "", GENERATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("parcelId");
        assertThatThrownBy(() -> MissionReport.generate(
                1L, 2L, null, BigDecimal.ONE, BigDecimal.ONE, "", GENERATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    void addsMetricsAndRejectsDuplicatedNames() {
        var report = report(BigDecimal.ONE, BigDecimal.ONE);

        report.addMetric("Flight time", new BigDecimal("42"), "min");

        assertThat(report.getMetrics()).singleElement().satisfies(metric -> {
            assertThat(metric.getName()).isEqualTo("Flight time");
            assertThat(metric.getUnit()).isEqualTo("min");
        });
        assertThat(report.hasMetric("flight TIME")).isTrue();
        assertThatThrownBy(() -> report.addMetric("flight time", BigDecimal.ONE, "min"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void exposesMetricsAsReadOnlyList() {
        var report = report(BigDecimal.ONE, BigDecimal.ONE);

        assertThatThrownBy(() -> report.getMetrics().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void calculatesApplicationRate() {
        assertThat(report(new BigDecimal("12.50"), new BigDecimal("38.00")).applicationRate())
                .isEqualByComparingTo("3.04");
        assertThat(report(BigDecimal.ZERO, new BigDecimal("10")).applicationRate()).isNull();
    }

    @Test
    void registersCreationEvent() {
        var report = MissionReport.reconstitute(
                7L, 1L, 2L, ReportType.MISSION_SUMMARY, BigDecimal.ONE, BigDecimal.ONE, "", GENERATED_AT, null);

        report.onCreated();

        assertThat(report.domainEvents()).singleElement().isEqualTo(
                new MissionReportGeneratedEvent(7L, 1L, 2L, ReportType.MISSION_SUMMARY));
    }
}
