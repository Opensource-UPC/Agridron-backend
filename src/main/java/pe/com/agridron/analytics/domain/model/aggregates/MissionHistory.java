package pe.com.agridron.analytics.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.analytics.domain.events.MissionHistoryRecordedEvent;
import pe.com.agridron.analytics.domain.model.services.DomainValidation;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Archived record of a finished mission. There is exactly one record per mission.
 */
@Getter
public class MissionHistory extends AbstractDomainAggregateRoot<MissionHistory> {

    private Long id;
    private final Long missionId;
    private final Long parcelId;
    private final MissionFinalStatus finalStatus;
    private final Instant completedAt;

    private MissionHistory(Long missionId, Long parcelId, MissionFinalStatus finalStatus, Instant completedAt) {
        this.missionId = DomainValidation.requirePositive(missionId, "missionId");
        this.parcelId = DomainValidation.requirePositive(parcelId, "parcelId");
        this.finalStatus = DomainValidation.requireNonNull(finalStatus, "finalStatus");
        this.completedAt = DomainValidation.requireNonNull(completedAt, "completedAt");
    }

    public static MissionHistory record(
            Long missionId, Long parcelId, MissionFinalStatus finalStatus, Instant completedAt) {
        return new MissionHistory(missionId, parcelId, finalStatus, completedAt);
    }

    public static MissionHistory reconstitute(
            Long id, Long missionId, Long parcelId, MissionFinalStatus finalStatus, Instant completedAt) {
        var history = new MissionHistory(missionId, parcelId, finalStatus, completedAt);
        history.id = DomainValidation.requirePositive(id, "id");
        return history;
    }

    public void onCreated() {
        registerDomainEvent(MissionHistoryRecordedEvent.from(this));
    }

    public boolean isCompletedOn(LocalDate day) {
        return completedAt.atZone(ZoneOffset.UTC).toLocalDate().equals(day);
    }
}
