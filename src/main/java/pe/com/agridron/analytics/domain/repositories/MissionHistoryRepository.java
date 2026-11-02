package pe.com.agridron.analytics.domain.repositories;

import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;

import java.util.List;
import java.util.Optional;

public interface MissionHistoryRepository {

    MissionHistory save(MissionHistory history);

    Optional<MissionHistory> findById(Long id);

    List<MissionHistory> search(MissionHistorySearchCriteria criteria);

    boolean existsByMissionId(Long missionId);
}
