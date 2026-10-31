package pe.com.agridron.analytics.domain.model.queries;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearchCriteriaTest {

    private static final Instant EARLY = Instant.parse("2026-09-01T00:00:00Z");
    private static final Instant LATE = Instant.parse("2026-10-01T00:00:00Z");

    @Test
    void emptyIdCollectionsDoNotFilter() {
        var criteria = new MissionReportSearchCriteria(List.of(), List.of(), null, null, null);

        assertThat(criteria.missionIds()).isNull();
        assertThat(criteria.parcelIds()).isNull();
    }

    @Test
    void rejectsNonPositiveIds() {
        assertThatThrownBy(() -> new MissionReportSearchCriteria(null, List.of(1L, 0L), null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("parcelIds");
        assertThatThrownBy(() -> new MissionHistorySearchCriteria(List.of(-1L), null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missionIds");
    }

    @Test
    void rejectsInvertedDateRange() {
        assertThatThrownBy(() -> new MissionReportSearchCriteria(null, null, null, LATE, EARLY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MissionHistorySearchCriteria(null, null, null, LATE, EARLY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noneCriteriaHasNoFilters() {
        assertThat(MissionReportSearchCriteria.none().type()).isNull();
        assertThat(MissionHistorySearchCriteria.none().status()).isNull();
    }
}
