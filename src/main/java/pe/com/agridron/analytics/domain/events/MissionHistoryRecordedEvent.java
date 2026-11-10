package pe.com.agridron.analytics.domain.events;

import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.shared.domain.events.DomainEvent;

public record MissionHistoryRecordedEvent(
        Long historyId,
        Long missionId,
        Long parcelId,
        MissionFinalStatus finalStatus) implements DomainEvent {

    public static MissionHistoryRecordedEvent from(MissionHistory history) {
        return new MissionHistoryRecordedEvent(
                history.getId(),
                history.getMissionId(),
                history.getParcelId(),
                history.getFinalStatus());
    }
}
