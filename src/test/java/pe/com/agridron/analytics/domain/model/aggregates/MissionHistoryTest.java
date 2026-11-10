package pe.com.agridron.analytics.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.com.agridron.analytics.domain.events.MissionHistoryRecordedEvent;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MissionHistoryTest {

    private static final Instant COMPLETED_AT = Instant.parse("2026-09-20T15:20:00Z");

    @Test
    void recordsFinishedMission() {
        var history = MissionHistory.record(1L, 2L, MissionFinalStatus.COMPLETED, COMPLETED_AT);

        assertThat(history.getId()).isNull();
        assertThat(history.getMissionId()).isEqualTo(1L);
        assertThat(history.getParcelId()).isEqualTo(2L);
        assertThat(history.getFinalStatus()).isEqualTo(MissionFinalStatus.COMPLETED);
        assertThat(history.getCompletedAt()).isEqualTo(COMPLETED_AT);
    }

    @Test
    void rejectsInvalidData() {
        assertThatThrownBy(() -> MissionHistory.record(0L, 2L, MissionFinalStatus.COMPLETED, COMPLETED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missionId");
        assertThatThrownBy(() -> MissionHistory.record(1L, 2L, null, COMPLETED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finalStatus");
        assertThatThrownBy(() -> MissionHistory.record(1L, 2L, MissionFinalStatus.CANCELLED, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("completedAt");
    }

    @Test
    void checksCompletionDayInUtc() {
        var history = MissionHistory.record(1L, 2L, MissionFinalStatus.COMPLETED, COMPLETED_AT);

        assertThat(history.isCompletedOn(LocalDate.of(2026, 9, 20))).isTrue();
        assertThat(history.isCompletedOn(LocalDate.of(2026, 9, 21))).isFalse();
    }

    @Test
    void registersCreationEvent() {
        var history = MissionHistory.reconstitute(9L, 1L, 2L, MissionFinalStatus.CANCELLED, COMPLETED_AT);

        history.onCreated();

        assertThat(history.domainEvents()).singleElement().isEqualTo(
                new MissionHistoryRecordedEvent(9L, 1L, 2L, MissionFinalStatus.CANCELLED));
    }
}
