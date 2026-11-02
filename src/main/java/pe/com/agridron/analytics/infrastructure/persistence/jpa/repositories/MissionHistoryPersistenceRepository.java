package pe.com.agridron.analytics.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.MissionHistoryPersistenceEntity;

@Repository
public interface MissionHistoryPersistenceRepository
        extends JpaRepository<MissionHistoryPersistenceEntity, Long>,
        JpaSpecificationExecutor<MissionHistoryPersistenceEntity> {

    boolean existsByMissionId(Long missionId);
}
