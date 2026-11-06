package pe.com.agridron.flight.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.flight.application.commandservices.MissionCommandService;
import pe.com.agridron.flight.domain.exceptions.InvalidMissionStateException;
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
import pe.com.agridron.flight.domain.model.valueobjects.AgrochemicalId;
import pe.com.agridron.flight.domain.model.valueobjects.DroneId;
import pe.com.agridron.flight.domain.model.valueobjects.FlightCoordinates;
import pe.com.agridron.flight.domain.model.valueobjects.FlightParameters;
import pe.com.agridron.flight.domain.model.valueobjects.OperatorId;
import pe.com.agridron.flight.domain.model.valueobjects.ParcelId;
import pe.com.agridron.flight.domain.repositories.MissionRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Transactional implementation of the mission command service.
 *
 * <p>Value objects are built here from the command primitives; invalid values raise
 * {@link IllegalArgumentException}, which the global handler maps to HTTP 400. Lifecycle
 * violations raised by the aggregate are translated into {@code MISSION_INVALID_STATE}.</p>
 */
@Service
@Transactional
public class MissionCommandServiceImpl implements MissionCommandService {

    private static final String MISSION = "Mission";

    private final MissionRepository missionRepository;

    public MissionCommandServiceImpl(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Override
    public Result<Mission, ApplicationError> handle(PlanMissionCommand command) {
        var mission = Mission.plan(new ParcelId(command.parcelId()), command.requestedDate());
        return Result.success(missionRepository.save(mission));
    }

    @Override
    public Result<Mission, ApplicationError> handle(AssignMissionResourcesCommand command) {
        var droneId = new DroneId(command.droneId());
        var agrochemicalId = new AgrochemicalId(command.agrochemicalId());
        return update(command.missionId(), mission -> mission.assignResources(droneId, agrochemicalId));
    }

    @Override
    public Result<Mission, ApplicationError> handle(AssignMissionOperatorCommand command) {
        var operatorId = new OperatorId(command.operatorId());
        return update(command.missionId(), mission -> mission.assignOperator(operatorId));
    }

    @Override
    public Result<Mission, ApplicationError> handle(ConfigureFlightParametersCommand command) {
        var parameters = new FlightParameters(
                command.flightHeight(),
                command.flightSpeed(),
                command.sprayRate(),
                command.overlap());
        return update(command.missionId(), mission -> mission.configureFlightParameters(parameters));
    }

    @Override
    public Result<Mission, ApplicationError> handle(RescheduleMissionCommand command) {
        return update(command.missionId(), mission -> mission.reschedule(command.requestedDate()));
    }

    @Override
    public Result<Mission, ApplicationError> handle(AuthorizeMissionCommand command) {
        return update(command.missionId(), Mission::authorize);
    }

    @Override
    public Result<Mission, ApplicationError> handle(StartMissionCommand command) {
        return update(command.missionId(), Mission::start);
    }

    @Override
    public Result<Mission, ApplicationError> handle(PauseMissionCommand command) {
        return update(command.missionId(), Mission::pause);
    }

    @Override
    public Result<Mission, ApplicationError> handle(ResumeMissionCommand command) {
        return update(command.missionId(), Mission::resume);
    }

    @Override
    public Result<Mission, ApplicationError> handle(CancelMissionCommand command) {
        return update(command.missionId(), Mission::cancel);
    }

    @Override
    public Result<Mission, ApplicationError> handle(CompleteMissionCommand command) {
        return update(command.missionId(), mission -> mission.complete(
                command.treatedArea(),
                command.appliedVolume(),
                command.observations()));
    }

    @Override
    public Result<Incident, ApplicationError> handle(ReportIncidentCommand command) {
        return update(
                command.missionId(),
                mission -> mission.reportIncident(command.type(), command.description(), command.occurredAt()),
                saved -> saved.latestIncident().orElseThrow());
    }

    @Override
    public Result<OperationStatus, ApplicationError> handle(UpdateOperationStatusCommand command) {
        var coordinates = new FlightCoordinates(command.latitude(), command.longitude());
        return update(
                command.missionId(),
                mission -> mission.updateOperationStatus(coordinates, command.state(), command.timestamp()),
                Mission::getOperationStatus);
    }

    private Result<Mission, ApplicationError> update(Long missionId, Consumer<Mission> action) {
        return update(missionId, action, Function.identity());
    }

    /**
     * Loads the mission, applies the action and saves it.
     *
     * @param extractor picks the value returned to the caller from the saved mission
     */
    private <T> Result<T, ApplicationError> update(
            Long missionId,
            Consumer<Mission> action,
            Function<Mission, T> extractor) {
        var mission = missionRepository.findById(missionId).orElse(null);
        if (mission == null) {
            return Result.failure(ApplicationError.notFound(MISSION, String.valueOf(missionId)));
        }
        try {
            action.accept(mission);
        } catch (InvalidMissionStateException ex) {
            return Result.failure(ApplicationError.invalidState(MISSION, ex.getMessage()));
        }
        return Result.success(extractor.apply(missionRepository.save(mission)));
    }
}
