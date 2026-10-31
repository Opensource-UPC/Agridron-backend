package pe.com.agridron.analytics.domain.model.aggregates;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceIndicatorTest {

    private static final Instant NOW = Instant.parse("2026-09-30T23:00:00Z");

    @Test
    void calculatesIndicatorForAPeriod() {
        var indicator = PerformanceIndicator.calculate("Hectares treated", new BigDecimal("20.70"), "ha", "2026-09", NOW);

        assertThat(indicator.getId()).isNull();
        assertThat(indicator.getName()).isEqualTo("Hectares treated");
        assertThat(indicator.getPeriod()).isEqualTo("2026-09");
        assertThat(indicator.getCalculatedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsInvalidPeriods() {
        assertThatThrownBy(() -> PerformanceIndicator.normalizePeriod("2026-13"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("YYYY-MM");
        assertThatThrownBy(() -> PerformanceIndicator.normalizePeriod("September"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PerformanceIndicator.normalizePeriod(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("period");
    }

    @Test
    void rejectsNegativeValues() {
        assertThatThrownBy(() -> PerformanceIndicator.calculate("Rate", new BigDecimal("-1"), "%", "2026-09", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");
    }

    @Test
    void recalculatesValueAndRegistersEvent() {
        var indicator = PerformanceIndicator.reconstitute(3L, "Completion rate", new BigDecimal("50"), "%", "2026-09", NOW);
        var later = NOW.plusSeconds(60);

        indicator.recalculate(new BigDecimal("75"), later);

        assertThat(indicator.getValue()).isEqualByComparingTo("75");
        assertThat(indicator.getCalculatedAt()).isEqualTo(later);
        assertThat(indicator.domainEvents()).hasSize(1);
    }
}
