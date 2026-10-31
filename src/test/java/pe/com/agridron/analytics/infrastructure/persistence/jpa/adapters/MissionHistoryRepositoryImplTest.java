package pe.com.agridron.analytics.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.analytics.support.AbstractAnalyticsPersistenceTest;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({MissionHistoryRepositoryImpl.class, DomainEventPublisher.class})
class MissionHistoryRepositoryImplTest extends AbstractAnalyticsPersistenceTest {

    @Autowired
    private MissionHistoryRepositoryImpl repository;

    @Autowired
    private TestEntityManager entityManager;

    private MissionHistory record(long missionId, long parcelId, MissionFinalStatus status, String completedAt) {
        return MissionHistory.record(missionId, parcelId, status, Instant.parse(completedAt));
    }

    @Test
    void savesAndFindsAHistoryRecord() {
        var saved = repository.save(record(1L, 2L, MissionFinalStatus.COMPLETED, "2026-09-20T15:20:00Z"));
        entityManager.clear();

        var found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMissionId()).isEqualTo(1L);
        assertThat(found.get().getFinalStatus()).isEqualTo(MissionFinalStatus.COMPLETED);
    }

    @Test
    void reportsExistenceByMission() {
        repository.save(record(1L, 2L, MissionFinalStatus.COMPLETED, "2026-09-20T15:20:00Z"));

        assertThat(repository.existsByMissionId(1L)).isTrue();
        assertThat(repository.existsByMissionId(2L)).isFalse();
    }

    @Test
    void filtersByParcelStatusAndDateRangeOrderedByMostRecent() {
        repository.save(record(1L, 2L, MissionFinalStatus.COMPLETED, "2026-09-20T15:20:00Z"));
        repository.save(record(2L, 3L, MissionFinalStatus.COMPLETED, "2026-09-25T11:00:00Z"));
        repository.save(record(3L, 2L, MissionFinalStatus.CANCELLED, "2026-10-02T09:00:00Z"));
        entityManager.clear();

        var all = repository.search(MissionHistorySearchCriteria.none());
        var parcelTwo = repository.search(new MissionHistorySearchCriteria(null, List.of(2L), null, null, null));
        var cancelled = repository.search(
                new MissionHistorySearchCriteria(null, null, MissionFinalStatus.CANCELLED, null, null));
        var september = repository.search(new MissionHistorySearchCriteria(
                null, null, null, Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2026-10-01T00:00:00Z")));

        assertThat(all).extracting(MissionHistory::getMissionId).containsExactly(3L, 2L, 1L);
        assertThat(parcelTwo).hasSize(2);
        assertThat(cancelled).singleElement().satisfies(history -> assertThat(history.getMissionId()).isEqualTo(3L));
        assertThat(september).hasSize(2);
    }
}
