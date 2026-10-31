package pe.com.agridron.analytics.domain.repositories;

import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;

import java.util.List;
import java.util.Optional;

public interface MissionReportRepository {

    MissionReport save(MissionReport report);

    Optional<MissionReport> findById(Long id);

    List<MissionReport> search(MissionReportSearchCriteria criteria);

    boolean existsByMissionIdAndType(Long missionId, ReportType type);

    boolean existsById(Long id);

    void deleteById(Long id);
}
