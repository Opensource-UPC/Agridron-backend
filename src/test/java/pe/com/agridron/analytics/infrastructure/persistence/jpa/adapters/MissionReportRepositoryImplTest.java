package pe.com.agridron.analytics.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pe.com.agridron.analytics.domain.events.MissionReportGeneratedEvent;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.analytics.support.AbstractAnalyticsPersistenceTest;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({MissionReportRepositoryImpl.class, DomainEventPublisher.class})
@RecordApplicationEvents
class MissionReportRepositoryImplTest extends AbstractAnalyticsPersistenceTest {

    private static final Instant SEPTEMBER = Instant.parse("2026-09-20T15:30:00Z");
    private static final Instant OCTOBER = Instant.parse("2026-10-02T09:00:00Z");

    @Autowired
    private MissionReportRepositoryImpl repository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private TestEntityManager entityManager;

    private MissionReport newReport(long missionId, long parcelId, ReportType type, Instant generatedAt) {
        return MissionReport.generate(
                missionId, parcelId, type, new BigDecimal("12.50"), new BigDecimal("38.00"), "ok", generatedAt);
    }

    @Test
    void savesNewReportAndPublishesCreationEvent() {
        var saved = repository.save(newReport(1L, 2L, ReportType.MISSION_SUMMARY, SEPTEMBER));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTreatedArea()).isEqualByComparingTo("12.50");
        assertThat(applicationEvents.stream(MissionReportGeneratedEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.reportId()).isEqualTo(saved.getId());
                    assertThat(event.missionId()).isEqualTo(1L);
                });
    }

    @Test
    void persistsMetricsAddedToAnExistingReport() {
        var saved = repository.save(newReport(1L, 2L, ReportType.MISSION_SUMMARY, SEPTEMBER));
        entityManager.clear();

        var loaded = repository.findById(saved.getId()).orElseThrow();
        loaded.addMetric("Flight time", new BigDecimal("42"), "min");
        repository.save(loaded);
        entityManager.clear();

        var reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getMetrics()).singleElement().satisfies(metric -> {
            assertThat(metric.getId()).isNotNull();
            assertThat(metric.getName()).isEqualTo("Flight time");
            assertThat(metric.getValue()).isEqualByComparingTo("42");
        });
    }

    @Test
    void filtersByParcelTypeAndDateRange() {
        repository.save(newReport(1L, 2L, ReportType.MISSION_SUMMARY, SEPTEMBER));
        repository.save(newReport(1L, 2L, ReportType.PHYTOSANITARY, SEPTEMBER));
        repository.save(newReport(2L, 3L, ReportType.MISSION_SUMMARY, OCTOBER));
        entityManager.clear();

        var onlyParcelTwo = repository.search(
                new MissionReportSearchCriteria(null, List.of(2L), null, null, null));
        var onlySummaries = repository.search(
                new MissionReportSearchCriteria(null, null, ReportType.MISSION_SUMMARY, null, null));
        var onlySeptember = repository.search(new MissionReportSearchCriteria(
                null, null, null, Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2026-10-01T00:00:00Z")));

        assertThat(onlyParcelTwo).hasSize(2);
        assertThat(onlySummaries).hasSize(2);
        assertThat(onlySeptember).hasSize(2);
    }

    @Test
    void reportsExistenceByMissionAndType() {
        repository.save(newReport(1L, 2L, ReportType.MISSION_SUMMARY, SEPTEMBER));

        assertThat(repository.existsByMissionIdAndType(1L, ReportType.MISSION_SUMMARY)).isTrue();
        assertThat(repository.existsByMissionIdAndType(1L, ReportType.PHYTOSANITARY)).isFalse();
    }

    @Test
    void deletesAReportTogetherWithItsMetrics() {
        var report = newReport(1L, 2L, ReportType.MISSION_SUMMARY, SEPTEMBER);
        report.addMetric("Flight time", new BigDecimal("42"), "min");
        var saved = repository.save(report);
        entityManager.clear();

        repository.deleteById(saved.getId());
        entityManager.flush();

        assertThat(repository.findById(saved.getId())).isEmpty();
        assertThat(repository.existsById(saved.getId())).isFalse();
    }
}
