package pe.com.agridron.analytics.infrastructure.persistence.jpa.assemblers;

import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.MissionHistoryPersistenceEntity;

public final class MissionHistoryPersistenceAssembler {

    private MissionHistoryPersistenceAssembler() {
    }

    public static MissionHistory toDomainFromPersistence(MissionHistoryPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return MissionHistory.reconstitute(
                entity.getId(),
                entity.getMissionId(),
                entity.getParcelId(),
                entity.getFinalStatus(),
                entity.getCompletedAt());
    }

    public static MissionHistoryPersistenceEntity toPersistenceFromDomain(MissionHistory history) {
        if (history == null) {
            return null;
        }
        var entity = new MissionHistoryPersistenceEntity(
                history.getMissionId(),
                history.getParcelId(),
                history.getFinalStatus(),
                history.getCompletedAt());
        entity.setId(history.getId());
        return entity;
    }
}
