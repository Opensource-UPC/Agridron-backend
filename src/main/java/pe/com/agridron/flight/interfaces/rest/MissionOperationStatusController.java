package pe.com.agridron.flight.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.flight.application.commandservices.MissionCommandService;
import pe.com.agridron.flight.application.queryservices.MissionQueryService;
import pe.com.agridron.flight.domain.model.queries.GetOperationStatusByMissionIdQuery;
import pe.com.agridron.flight.interfaces.rest.resources.OperationStatusResource;
import pe.com.agridron.flight.interfaces.rest.resources.UpdateOperationStatusResource;
import pe.com.agridron.flight.interfaces.rest.transform.OperationStatusResourceFromEntityAssembler;
import pe.com.agridron.flight.interfaces.rest.transform.UpdateOperationStatusCommandFromResourceAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

/**
 * REST controller for the last operational status (telemetry) of a mission.
 */
@RestController
@RequestMapping("/api/v1/missions/{missionId}/operation-status")
@Tag(name = "Missions", description = "Mission planning and lifecycle endpoints")
public class MissionOperationStatusController {

    private final MissionCommandService missionCommandService;
    private final MissionQueryService missionQueryService;

    public MissionOperationStatusController(
            MissionCommandService missionCommandService,
            MissionQueryService missionQueryService) {
        this.missionCommandService = missionCommandService;
        this.missionQueryService = missionQueryService;
    }

    @PutMapping
    @Operation(
            summary = "Report telemetry",
            description = "Records the drone position and flight phase of an IN_PROGRESS or PAUSED mission. "
                    + "Reports older than the current one are ignored.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation status recorded",
                    content = @Content(schema = @Schema(implementation = OperationStatusResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Mission is not being flown")
    })
    public ResponseEntity<?> updateOperationStatus(
            @PathVariable @Parameter(description = "Mission identifier", example = "1") Long missionId,
            @Valid @RequestBody UpdateOperationStatusResource resource) {
        var command = UpdateOperationStatusCommandFromResourceAssembler.toCommandFromResource(missionId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                missionCommandService.handle(command),
                OperationStatusResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Get the operation status", description = "Retrieves the last telemetry report of a mission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation status found",
                    content = @Content(schema = @Schema(implementation = OperationStatusResource.class))),
            @ApiResponse(responseCode = "404", description = "Mission not found or no telemetry received yet")
    })
    public ResponseEntity<?> getOperationStatus(
            @PathVariable @Parameter(description = "Mission identifier", example = "1") Long missionId) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                missionQueryService.handle(new GetOperationStatusByMissionIdQuery(missionId)),
                OperationStatusResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}
