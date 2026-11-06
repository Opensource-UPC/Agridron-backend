package pe.com.agridron.flight.application.queryservices;

import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.entities.Incident;
import pe.com.agridron.flight.domain.model.entities.OperationStatus;
import pe.com.agridron.flight.domain.model.queries.ExistsActiveMissionByParcelIdQuery;
import pe.com.agridron.flight.domain.model.queries.GetAllMissionsQuery;
import pe.com.agridron.flight.domain.model.queries.GetIncidentsByMissionIdQuery;
import pe.com.agridron.flight.domain.model.queries.GetMissionByIdQuery;
import pe.com.agridron.flight.domain.model.queries.GetOperationStatusByMissionIdQuery;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Application service that resolves mission read queries.
 */
public interface MissionQueryService {

    /**
     * Retrieves a mission by its identifier.
     *
     * @return the mission or a {@code MISSION_NOT_FOUND} error
     */
    Result<Mission, ApplicationError> handle(GetMissionByIdQuery query);

    /**
     * Lists missions, optionally filtered by parcel and status.
     *
     * @return the matching missions, possibly empty
     */
    List<Mission> handle(GetAllMissionsQuery query);

    /**
     * Lists the incidents of a mission in reporting order.
     *
     * @return the incidents or a {@code MISSION_NOT_FOUND} error
     */
    Result<List<Incident>, ApplicationError> handle(GetIncidentsByMissionIdQuery query);

    /**
     * Retrieves the last operational status of a mission.
     *
     * @return the status, {@code MISSION_NOT_FOUND}, or {@code OPERATION_STATUS_NOT_FOUND}
     *         when no telemetry has been received yet
     */
    Result<OperationStatus, ApplicationError> handle(GetOperationStatusByMissionIdQuery query);

    /**
     * Checks whether a parcel takes part in a mission that has not finished.
     */
    boolean handle(ExistsActiveMissionByParcelIdQuery query);
}
