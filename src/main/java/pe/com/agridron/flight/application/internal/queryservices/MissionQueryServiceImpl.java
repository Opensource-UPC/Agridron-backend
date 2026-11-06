package pe.com.agridron.flight.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.flight.application.queryservices.MissionQueryService;
import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.entities.Incident;
import pe.com.agridron.flight.domain.model.entities.OperationStatus;
import pe.com.agridron.flight.domain.model.queries.ExistsActiveMissionByParcelIdQuery;
import pe.com.agridron.flight.domain.model.queries.GetAllMissionsQuery;
import pe.com.agridron.flight.domain.model.queries.GetIncidentsByMissionIdQuery;
import pe.com.agridron.flight.domain.model.queries.GetMissionByIdQuery;
import pe.com.agridron.flight.domain.model.queries.GetOperationStatusByMissionIdQuery;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;
import pe.com.agridron.flight.domain.repositories.MissionRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.Arrays;
import java.util.List;

/**
 * Read-only implementation of the mission query service.
 */
@Service
@Transactional(readOnly = true)
public class MissionQueryServiceImpl implements MissionQueryService {

    private static final List<MissionStatus> ACTIVE_STATUSES = Arrays.stream(MissionStatus.values())
            .filter(MissionStatus::isActive)
            .toList();

    private final MissionRepository missionRepository;

    public MissionQueryServiceImpl(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Override
    public Result<Mission, ApplicationError> handle(GetMissionByIdQuery query) {
        return findMission(query.missionId());
    }

    @Override
    public List<Mission> handle(GetAllMissionsQuery query) {
        return missionRepository.findAll(query.parcelId(), query.status());
    }

    @Override
    public Result<List<Incident>, ApplicationError> handle(GetIncidentsByMissionIdQuery query) {
        return findMission(query.missionId()).map(Mission::getIncidents);
    }

    @Override
    public Result<OperationStatus, ApplicationError> handle(GetOperationStatusByMissionIdQuery query) {
        return findMission(query.missionId()).flatMap(mission -> mission.getOperationStatus() == null
                ? Result.failure(ApplicationError.notFound(
                        "OperationStatus", "mission " + query.missionId()))
                : Result.success(mission.getOperationStatus()));
    }

    @Override
    public boolean handle(ExistsActiveMissionByParcelIdQuery query) {
        return missionRepository.existsByParcelIdAndStatusIn(query.parcelId(), ACTIVE_STATUSES);
    }

    private Result<Mission, ApplicationError> findMission(Long missionId) {
        return missionRepository.findById(missionId)
                .<Result<Mission, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("Mission", String.valueOf(missionId))));
    }
}
