package pe.com.agridron.analytics.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.domain.model.commands.CalculatePerformanceIndicatorsCommand;
import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.analytics.domain.repositories.MissionHistoryRepository;
import pe.com.agridron.analytics.domain.repositories.MissionReportRepository;
import pe.com.agridron.analytics.domain.repositories.PerformanceIndicatorRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceIndicatorCommandServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-30T23:00:00Z");

    @Mock
    private MissionHistoryRepository historyRepository;

    @Mock
    private MissionReportRepository reportRepository;

    @Mock
    private PerformanceIndicatorRepository indicatorRepository;

    private PerformanceIndicatorCommandServiceImpl service() {
        return new PerformanceIndicatorCommandServiceImpl(historyRepository, reportRepository, indicatorRepository);
    }

    @Test
    void rejectsInvalidPeriod() {
        var result = service().handle(new CalculatePerformanceIndicatorsCommand("2026-13", NOW));

        assertThat(result.<String>fold(list -> "OK", error -> error.code())).isEqualTo("VALIDATION_ERROR");
        verify(indicatorRepository, never()).save(any());
    }

    @Test
    void returnsNoIndicatorsWhenThePeriodHasNoHistory() {
        when(historyRepository.search(any(MissionHistorySearchCriteria.class))).thenReturn(List.of());

        var result = service().handle(new CalculatePerformanceIndicatorsCommand("2026-09", NOW));

        assertThat(result.toOptional()).contains(List.of());
        verify(indicatorRepository, never()).save(any());
    }

    @Test
    void calculatesOneIndicatorPerMetricAndSavesThem() {
        var history = MissionHistory.reconstitute(1L, 10L, 2L, MissionFinalStatus.COMPLETED, NOW);
        var report = MissionReport.reconstitute(
                1L, 10L, 2L, ReportType.MISSION_SUMMARY,
                new BigDecimal("10.00"), new BigDecimal("30.00"), "", NOW, null);
        when(historyRepository.search(any(MissionHistorySearchCriteria.class))).thenReturn(List.of(history));
        when(reportRepository.search(any(MissionReportSearchCriteria.class))).thenReturn(List.of(report));
        when(indicatorRepository.findByNameAndPeriod(anyString(), anyString())).thenReturn(Optional.empty());
        when(indicatorRepository.save(any(PerformanceIndicator.class))).thenAnswer(call -> call.getArgument(0));

        var result = service().handle(new CalculatePerformanceIndicatorsCommand("2026-09", NOW));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrElse(List.of()))
                .extracting(PerformanceIndicator::getName)
                .containsExactly("Hectares treated", "Completion rate", "Applied volume", "Application rate");
    }

    @Test
    void searchesTheHistoryOfTheWholeMonthInUtc() {
        when(historyRepository.search(any(MissionHistorySearchCriteria.class))).thenReturn(List.of());

        service().handle(new CalculatePerformanceIndicatorsCommand("2026-09", NOW));

        var captor = ArgumentCaptor.forClass(MissionHistorySearchCriteria.class);
        verify(historyRepository).search(captor.capture());
        assertThat(captor.getValue().from()).isEqualTo(Instant.parse("2026-09-01T00:00:00Z"));
        assertThat(captor.getValue().to()).isEqualTo(Instant.parse("2026-10-01T00:00:00Z"));
    }

    @Test
    void recalculatesAnExistingIndicator() {
        var history = MissionHistory.reconstitute(1L, 10L, 2L, MissionFinalStatus.COMPLETED, NOW);
        var existing = PerformanceIndicator.reconstitute(
                7L, "Hectares treated", new BigDecimal("1.00"), "ha", "2026-09", NOW.minusSeconds(3600));
        when(historyRepository.search(any(MissionHistorySearchCriteria.class))).thenReturn(List.of(history));
        when(reportRepository.search(any(MissionReportSearchCriteria.class))).thenReturn(List.of());
        when(indicatorRepository.findByNameAndPeriod(anyString(), anyString())).thenReturn(Optional.empty());
        when(indicatorRepository.findByNameAndPeriod("Hectares treated", "2026-09")).thenReturn(Optional.of(existing));
        when(indicatorRepository.save(any(PerformanceIndicator.class))).thenAnswer(call -> call.getArgument(0));

        service().handle(new CalculatePerformanceIndicatorsCommand("2026-09", NOW));

        assertThat(existing.getId()).isEqualTo(7L);
        assertThat(existing.getCalculatedAt()).isEqualTo(NOW);
        assertThat(existing.getValue()).isEqualByComparingTo("0");
    }
}
