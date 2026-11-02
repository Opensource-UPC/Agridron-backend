package pe.com.agridron.analytics.application.internal.queryservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.queries.GetMissionReportByIdQuery;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.analytics.domain.repositories.MissionReportRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionReportQueryServiceImplTest {

    @Mock
    private MissionReportRepository repository;

    @InjectMocks
    private MissionReportQueryServiceImpl service;

    private static MissionReport report() {
        return MissionReport.reconstitute(
                5L, 1L, 2L, ReportType.MISSION_SUMMARY, BigDecimal.ONE, BigDecimal.ONE, "",
                Instant.parse("2026-09-20T15:30:00Z"), null);
    }

    @Test
    void findsAReportById() {
        var report = report();
        when(repository.findById(5L)).thenReturn(Optional.of(report));

        var result = service.handle(new GetMissionReportByIdQuery(5L));

        assertThat(result.toOptional()).contains(report);
    }

    @Test
    void returnsNotFoundForUnknownReport() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        var result = service.handle(new GetMissionReportByIdQuery(99L));

        assertThat(result.<String>fold(r -> "OK", error -> error.code())).isEqualTo("REPORT_NOT_FOUND");
    }

    @Test
    void delegatesSearchToTheRepository() {
        var criteria = MissionReportSearchCriteria.none();
        when(repository.search(criteria)).thenReturn(List.of(report()));

        assertThat(service.handle(criteria)).hasSize(1);
    }
}
