package pe.com.agridron.analytics.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.commands.AddOperationalMetricCommand;
import pe.com.agridron.analytics.domain.model.commands.DeleteMissionReportCommand;
import pe.com.agridron.analytics.domain.model.commands.GenerateMissionReportCommand;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.analytics.domain.repositories.MissionReportRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionReportCommandServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-20T15:30:00Z");

    @Mock
    private MissionReportRepository repository;

    @InjectMocks
    private MissionReportCommandServiceImpl service;

    private static GenerateMissionReportCommand generateCommand(Long missionId, BigDecimal area) {
        return new GenerateMissionReportCommand(
                missionId, 2L, ReportType.MISSION_SUMMARY, area, new BigDecimal("38"), "ok", NOW);
    }

    private static MissionReport persistedReport() {
        return MissionReport.reconstitute(
                5L, 1L, 2L, ReportType.MISSION_SUMMARY, BigDecimal.ONE, BigDecimal.ONE, "", NOW, null);
    }

    @Test
    void generatesAndSavesAReport() {
        var saved = persistedReport();
        when(repository.existsByMissionIdAndType(1L, ReportType.MISSION_SUMMARY)).thenReturn(false);
        when(repository.save(any(MissionReport.class))).thenReturn(saved);

        var result = service.handle(generateCommand(1L, new BigDecimal("12.5")));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional()).contains(saved);
    }

    @Test
    void rejectsDuplicatedReportOfTheSameType() {
        when(repository.existsByMissionIdAndType(1L, ReportType.MISSION_SUMMARY)).thenReturn(true);

        var result = service.handle(generateCommand(1L, new BigDecimal("12.5")));

        assertThat(result.<String>fold(report -> "OK", error -> error.code())).isEqualTo("REPORT_CONFLICT");
        verify(repository, never()).save(any());
    }

    @Test
    void returnsValidationErrorForInvalidData() {
        var result = service.handle(generateCommand(1L, new BigDecimal("-1")));

        assertThat(result.<String>fold(report -> "OK", error -> error.code())).isEqualTo("VALIDATION_ERROR");
        verify(repository, never()).save(any());
    }

    @Test
    void addsAMetricToAnExistingReport() {
        var report = persistedReport();
        when(repository.findById(5L)).thenReturn(Optional.of(report));
        when(repository.save(report)).thenReturn(report);

        var result = service.handle(new AddOperationalMetricCommand(5L, "Flight time", new BigDecimal("42"), "min"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(report.getMetrics()).hasSize(1);
    }

    @Test
    void rejectsDuplicatedMetricName() {
        var report = persistedReport();
        report.addMetric("Flight time", BigDecimal.ONE, "min");
        when(repository.findById(5L)).thenReturn(Optional.of(report));

        var result = service.handle(new AddOperationalMetricCommand(5L, "flight time", BigDecimal.TEN, "min"));

        assertThat(result.<String>fold(r -> "OK", error -> error.code())).isEqualTo("REPORT_CONFLICT");
        verify(repository, never()).save(any());
    }

    @Test
    void reportsMissingReportWhenAddingMetric() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        var result = service.handle(new AddOperationalMetricCommand(99L, "Flight time", BigDecimal.ONE, "min"));

        assertThat(result.<String>fold(r -> "OK", error -> error.code())).isEqualTo("REPORT_NOT_FOUND");
    }

    @Test
    void returnsValidationErrorForInvalidMetric() {
        var report = persistedReport();
        when(repository.findById(5L)).thenReturn(Optional.of(report));

        var result = service.handle(new AddOperationalMetricCommand(5L, "Flight time", BigDecimal.ONE, " "));

        assertThat(result.<String>fold(r -> "OK", error -> error.code())).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void deletesAnExistingReport() {
        when(repository.existsById(5L)).thenReturn(true);

        var result = service.handle(new DeleteMissionReportCommand(5L));

        assertThat(result.toOptional()).contains(5L);
        verify(repository).deleteById(5L);
    }

    @Test
    void reportsMissingReportWhenDeleting() {
        when(repository.existsById(99L)).thenReturn(false);

        var result = service.handle(new DeleteMissionReportCommand(99L));

        assertThat(result.<String>fold(r -> "OK", error -> error.code())).isEqualTo("REPORT_NOT_FOUND");
        verify(repository, never()).deleteById(any());
    }
}
