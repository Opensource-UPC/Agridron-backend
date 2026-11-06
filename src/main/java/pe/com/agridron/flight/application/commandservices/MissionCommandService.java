package pe.com.agridron.flight.application.commandservices;

import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.commands.AssignMissionOperatorCommand;
import pe.com.agridron.flight.domain.model.commands.AssignMissionResourcesCommand;
import pe.com.agridron.flight.domain.model.commands.AuthorizeMissionCommand;
import pe.com.agridron.flight.domain.model.commands.CancelMissionCommand;
import pe.com.agridron.flight.domain.model.commands.CompleteMissionCommand;
import pe.com.agridron.flight.domain.model.commands.ConfigureFlightParametersCommand;
import pe.com.agridron.flight.domain.model.commands.PauseMissionCommand;
import pe.com.agridron.flight.domain.model.commands.PlanMissionCommand;
import pe.com.agridron.flight.domain.model.commands.ReportIncidentCommand;
import pe.com.agridron.flight.domain.model.commands.RescheduleMissionCommand;
import pe.com.agridron.flight.domain.model.commands.ResumeMissionCommand;
import pe.com.agridron.flight.domain.model.commands.StartMissionCommand;
import pe.com.agridron.flight.domain.model.commands.UpdateOperationStatusCommand;
import pe.com.agridron.flight.domain.model.entities.Incident;
import pe.com.agridron.flight.domain.model.entities.OperationStatus;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Application service that handles mission write commands.
 *
 * <p>Every operation on an existing mission returns {@code MISSION_NOT_FOUND} when the
 * mission does not exist and {@code MISSION_INVALID_STATE} when the lifecycle does not
 * allow it.</p>
 */
public interface MissionCommandService {

    /** Plans a new mission over a parcel. */
    Result<Mission, ApplicationError> handle(PlanMissionCommand command);

    /** Assigns the drone and the agrochemical of a planned mission. */
    Result<Mission, ApplicationError> handle(AssignMissionResourcesCommand command);

    /** Assigns the operator of a planned mission. */
    Result<Mission, ApplicationError> handle(AssignMissionOperatorCommand command);

    /** Configures the flight parameters of a planned mission. */
    Result<Mission, ApplicationError> handle(ConfigureFlightParametersCommand command);

    /** Moves a planned or authorized mission to another date. */
    Result<Mission, ApplicationError> handle(RescheduleMissionCommand command);

    /** Authorizes a fully planned mission. */
    Result<Mission, ApplicationError> handle(AuthorizeMissionCommand command);

    /** Starts an authorized mission. */
    Result<Mission, ApplicationError> handle(StartMissionCommand command);

    /** Pauses a mission in progress. */
    Result<Mission, ApplicationError> handle(PauseMissionCommand command);

    /** Resumes a paused mission. */
    Result<Mission, ApplicationError> handle(ResumeMissionCommand command);

    /** Cancels a mission that has not finished. */
    Result<Mission, ApplicationError> handle(CancelMissionCommand command);

    /** Completes a mission in progress and registers its result. */
    Result<Mission, ApplicationError> handle(CompleteMissionCommand command);

    /** Reports an incident during a mission in progress or paused. */
    Result<Incident, ApplicationError> handle(ReportIncidentCommand command);

    /** Records a telemetry report of a mission in progress or paused. */
    Result<OperationStatus, ApplicationError> handle(UpdateOperationStatusCommand command);
}
