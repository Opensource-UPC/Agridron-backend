package pe.com.agridron.analytics.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.MissionReportPersistenceEntity;

@Repository
public interface MissionReportPersistenceRepository
        extends JpaRepository<MissionReportPersistenceEntity, Long>,
        JpaSpecificationExecutor<MissionReportPersistenceEntity> {

    boolean existsByMissionIdAndType(Long missionId, ReportType type);
}
