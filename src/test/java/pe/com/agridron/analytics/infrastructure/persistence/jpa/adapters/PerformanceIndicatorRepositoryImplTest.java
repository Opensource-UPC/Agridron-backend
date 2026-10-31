package pe.com.agridron.analytics.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.support.AbstractAnalyticsPersistenceTest;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Import({PerformanceIndicatorRepositoryImpl.class, DomainEventPublisher.class})
class PerformanceIndicatorRepositoryImplTest extends AbstractAnalyticsPersistenceTest {

    private static final Instant NOW = Instant.parse("2026-09-30T23:00:00Z");

    @Autowired
    private PerformanceIndicatorRepositoryImpl repository;

    @Autowired
    private TestEntityManager entityManager;

    private PerformanceIndicator indicator(String name, String value, String period) {
        return PerformanceIndicator.calculate(name, new BigDecimal(value), "ha", period, NOW);
    }

    @Test
    void savesAndFindsByNameAndPeriod() {
        repository.save(indicator("Hectares treated", "20.70", "2026-09"));
        entityManager.clear();

        var found = repository.findByNameAndPeriod("Hectares treated", "2026-09");

        assertThat(found).isPresent();
        assertThat(found.get().getValue()).isEqualByComparingTo("20.70");
        assertThat(repository.findByNameAndPeriod("Hectares treated", "2026-10")).isEmpty();
    }

    @Test
    void updatesAnExistingIndicatorInsteadOfCreatingAnotherOne() {
        var saved = repository.save(indicator("Hectares treated", "20.70", "2026-09"));
        entityManager.clear();

        var existing = repository.findByNameAndPeriod("Hectares treated", "2026-09").orElseThrow();
        existing.recalculate(new BigDecimal("25.00"), NOW.plusSeconds(60));
        var updated = repository.save(existing);
        entityManager.clear();

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(repository.findByPeriod("2026-09")).singleElement()
                .satisfies(found -> assertThat(found.getValue()).isEqualByComparingTo("25.00"));
    }

    @Test
    void listsIndicatorsByPeriodAndAll() {
        repository.save(indicator("Hectares treated", "20.70", "2026-09"));
        repository.save(indicator("Applied volume", "62.60", "2026-09"));
        repository.save(indicator("Hectares treated", "5.00", "2026-10"));
        entityManager.clear();

        assertThat(repository.findByPeriod("2026-09"))
                .extracting(PerformanceIndicator::getName)
                .containsExactly("Applied volume", "Hectares treated");
        assertThat(repository.findAll()).hasSize(3);
    }
}
