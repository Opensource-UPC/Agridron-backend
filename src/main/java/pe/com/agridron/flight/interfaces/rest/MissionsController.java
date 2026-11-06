package pe.com.agridron.flight.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.flight.application.commandservices.MissionCommandService;
import pe.com.agridron.flight.application.queryservices.MissionQueryService;
import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.commands.AssignMissionOperatorCommand;
import pe.com.agridron.flight.domain.model.commands.AssignMissionResourcesCommand;
import pe.com.agridron.flight.domain.model.commands.AuthorizeMissionCommand;
import pe.com.agridron.flight.domain.model.commands.CancelMissionCommand;
import pe.com.agridron.flight.domain.model.commands.PauseMissionCommand;
import pe.com.agridron.flight.domain.model.commands.RescheduleMissionCommand;
import pe.com.agridron.flight.domain.model.commands.ResumeMissionCommand;
import pe.com.agridron.flight.domain.model.commands.StartMissionCommand;
import pe.com.agridron.flight.domain.model.queries.GetAllMissionsQuery;
import pe.com.agridron.flight.domain.model.queries.GetMissionByIdQuery;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;
import pe.com.agridron.flight.interfaces.rest.resources.AssignMissionOperatorResource;
import pe.com.agridron.flight.interfaces.rest.resources.AssignMissionResourcesResource;
import pe.com.agridron.flight.interfaces.rest.resources.CompleteMissionResource;
import pe.com.agridron.flight.interfaces.rest.resources.FlightParametersResource;
import pe.com.agridron.flight.interfaces.rest.resources.MissionResource;
import pe.com.agridron.flight.interfaces.rest.resources.PlanMissionResource;
import pe.com.agridron.flight.interfaces.rest.resources.RescheduleMissionResource;
import pe.com.agridron.flight.interfaces.rest.transform.CompleteMissionCommandFromResourceAssembler;
import pe.com.agridron.flight.interfaces.rest.transform.ConfigureFlightParametersCommandFromResourceAssembler;
import pe.com.agridron.flight.interfaces.rest.transform.MissionResourceFromEntityAssembler;
import pe.com.agridron.flight.interfaces.rest.transform.PlanMissionCommandFromResourceAssembler;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

/**
 * REST controller that exposes mission planning and lifecycle endpoints.
 *
 * <p>Lifecycle transitions are modelled as action sub-resources
 * ({@code POST /missions/{id}/authorize}, {@code /start}, ...). An invalid transition returns
 * {@code 422 MISSION_INVALID_STATE}.</p>
 */
@RestController
@RequestMapping("/api/v1/missions")
@Tag(name = "Missions", description = "Mission planning and lifecycle endpoints")
public class MissionsController {

    private final MissionCommandService missionCommandService;
    private final MissionQueryService missionQueryService;

    public MissionsController(
            MissionCommandService missionCommandService,
            MissionQueryService missionQueryService) {
        this.missionCommandService = missionCommandService;
        this.missionQueryService = missionQueryService;
    }

    // ---------------------------------------------------------------- planning

    @PostMapping
    @Operation(summary = "Plan a mission", description = "Plans a new mission over a parcel in status PLANNED.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Mission planned",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<?> planMission(@Valid @RequestBody PlanMissionResource resource) {
        var command = PlanMissionCommandFromResourceAssembler.toCommandFromResource(resource);
        return toResponse(missionCommandService.handle(command), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List missions",
            description = "Lists missions ordered by requested date, optionally filtered by parcel and status.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Missions retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MissionResource.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters")
    })
    public ResponseEntity<?> getAllMissions(
            @RequestParam(required = false)
            @Parameter(description = "Parcel identifier", example = "1")
            Long parcelId,
            @RequestParam(required = false)
            @Parameter(description = "Lifecycle status", example = "PLANNED")
            MissionStatus status) {
        var missions = missionQueryService.handle(new GetAllMissionsQuery(parcelId, status));
        return ResponseEntity.ok(missions.stream()
                .map(MissionResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @GetMapping("/{missionId}")
    @Operation(summary = "Get a mission by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Mission found",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "404", description = "Mission not found")
    })
    public ResponseEntity<?> getMissionById(@PathVariable @Parameter(description = "Mission identifier", example = "1") Long missionId) {
        return toResponse(missionQueryService.handle(new GetMissionByIdQuery(missionId)), HttpStatus.OK);
    }

    @PutMapping("/{missionId}/resources")
    @Operation(summary = "Assign resources", description = "Assigns the drone and agrochemical of a PLANNED mission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resources assigned",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Mission is not PLANNED")
    })
    public ResponseEntity<?> assignResources(
            @PathVariable Long missionId,
            @Valid @RequestBody AssignMissionResourcesResource resource) {
        var command = new AssignMissionResourcesCommand(missionId, resource.droneId(), resource.agrochemicalId());
        return toResponse(missionCommandService.handle(command), HttpStatus.OK);
    }

    @PutMapping("/{missionId}/operator")
    @Operation(summary = "Assign operator", description = "Assigns the operator of a PLANNED mission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operator assigned",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Mission is not PLANNED")
    })
    public ResponseEntity<?> assignOperator(
            @PathVariable Long missionId,
            @Valid @RequestBody AssignMissionOperatorResource resource) {
        var command = new AssignMissionOperatorCommand(missionId, resource.operatorId());
        return toResponse(missionCommandService.handle(command), HttpStatus.OK);
    }

    @PutMapping("/{missionId}/flight-parameters")
    @Operation(summary = "Configure flight parameters", description = "Configures the flight parameters of a PLANNED mission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight parameters configured",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Mission is not PLANNED")
    })
    public ResponseEntity<?> configureFlightParameters(
            @PathVariable Long missionId,
            @Valid @RequestBody FlightParametersResource resource) {
        var command = ConfigureFlightParametersCommandFromResourceAssembler.toCommandFromResource(missionId, resource);
        return toResponse(missionCommandService.handle(command), HttpStatus.OK);
    }

    @PutMapping("/{missionId}/schedule")
    @Operation(summary = "Reschedule a mission", description = "Moves a PLANNED or AUTHORIZED mission to another date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mission rescheduled",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Mission is neither PLANNED nor AUTHORIZED")
    })
    public ResponseEntity<?> reschedule(
            @PathVariable Long missionId,
            @Valid @RequestBody RescheduleMissionResource resource) {
        var command = new RescheduleMissionCommand(missionId, resource.requestedDate());
        return toResponse(missionCommandService.handle(command), HttpStatus.OK);
    }

    // ---------------------------------------------------------------- lifecycle

    @PostMapping("/{missionId}/authorize")
    @Operation(summary = "Authorize a mission",
            description = "PLANNED → AUTHORIZED. Requires drone, agrochemical, operator and flight parameters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mission authorized",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Invalid transition or missing resources")
    })
    public ResponseEntity<?> authorize(@PathVariable Long missionId) {
        return toResponse(missionCommandService.handle(new AuthorizeMissionCommand(missionId)), HttpStatus.OK);
    }

    @PostMapping("/{missionId}/start")
    @Operation(summary = "Start a mission", description = "AUTHORIZED → IN_PROGRESS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mission started",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Invalid transition")
    })
    public ResponseEntity<?> start(@PathVariable Long missionId) {
        return toResponse(missionCommandService.handle(new StartMissionCommand(missionId)), HttpStatus.OK);
    }

    @PostMapping("/{missionId}/pause")
    @Operation(summary = "Pause a mission", description = "IN_PROGRESS → PAUSED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mission paused",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Invalid transition")
    })
    public ResponseEntity<?> pause(@PathVariable Long missionId) {
        return toResponse(missionCommandService.handle(new PauseMissionCommand(missionId)), HttpStatus.OK);
    }

    @PostMapping("/{missionId}/resume")
    @Operation(summary = "Resume a mission", description = "PAUSED → IN_PROGRESS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mission resumed",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Invalid transition")
    })
    public ResponseEntity<?> resume(@PathVariable Long missionId) {
        return toResponse(missionCommandService.handle(new ResumeMissionCommand(missionId)), HttpStatus.OK);
    }

    @PostMapping("/{missionId}/cancel")
    @Operation(summary = "Cancel a mission", description = "Any non-terminal status → CANCELLED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mission cancelled",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Mission already COMPLETED or CANCELLED")
    })
    public ResponseEntity<?> cancel(@PathVariable Long missionId) {
        return toResponse(missionCommandService.handle(new CancelMissionCommand(missionId)), HttpStatus.OK);
    }

    @PostMapping("/{missionId}/complete")
    @Operation(summary = "Complete a mission", description = "IN_PROGRESS → COMPLETED, registering the mission result.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mission completed",
                    content = @Content(schema = @Schema(implementation = MissionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Invalid transition")
    })
    public ResponseEntity<?> complete(
            @PathVariable Long missionId,
            @Valid @RequestBody CompleteMissionResource resource) {
        var command = CompleteMissionCommandFromResourceAssembler.toCommandFromResource(missionId, resource);
        return toResponse(missionCommandService.handle(command), HttpStatus.OK);
    }

    private static ResponseEntity<?> toResponse(Result<Mission, ApplicationError> result, HttpStatus status) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MissionResourceFromEntityAssembler::toResourceFromEntity,
                status);
    }
}
