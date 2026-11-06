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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.flight.application.commandservices.MissionCommandService;
import pe.com.agridron.flight.application.queryservices.MissionQueryService;
import pe.com.agridron.flight.domain.model.queries.GetIncidentsByMissionIdQuery;
import pe.com.agridron.flight.interfaces.rest.resources.IncidentResource;
import pe.com.agridron.flight.interfaces.rest.resources.ReportIncidentResource;
import pe.com.agridron.flight.interfaces.rest.transform.IncidentResourceFromEntityAssembler;
import pe.com.agridron.flight.interfaces.rest.transform.ReportIncidentCommandFromResourceAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

/**
 * REST controller for the incidents of a mission.
 */
@RestController
@RequestMapping("/api/v1/missions/{missionId}/incidents")
@Tag(name = "Missions", description = "Mission planning and lifecycle endpoints")
public class MissionIncidentsController {

    private final MissionCommandService missionCommandService;
    private final MissionQueryService missionQueryService;

    public MissionIncidentsController(
            MissionCommandService missionCommandService,
            MissionQueryService missionQueryService) {
        this.missionCommandService = missionCommandService;
        this.missionQueryService = missionQueryService;
    }

    @PostMapping
    @Operation(summary = "Report an incident", description = "Reports an incident during an IN_PROGRESS or PAUSED mission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Incident reported",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Mission not found"),
            @ApiResponse(responseCode = "422", description = "Mission is not being flown")
    })
    public ResponseEntity<?> reportIncident(
            @PathVariable @Parameter(description = "Mission identifier", example = "1") Long missionId,
            @Valid @RequestBody ReportIncidentResource resource) {
        var command = ReportIncidentCommandFromResourceAssembler.toCommandFromResource(missionId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                missionCommandService.handle(command),
                incident -> IncidentResourceFromEntityAssembler.toResourceFromEntity(missionId, incident),
                HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List incidents", description = "Lists the incidents of a mission in reporting order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidents retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = IncidentResource.class)))),
            @ApiResponse(responseCode = "404", description = "Mission not found")
    })
    public ResponseEntity<?> getIncidents(
            @PathVariable @Parameter(description = "Mission identifier", example = "1") Long missionId) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                missionQueryService.handle(new GetIncidentsByMissionIdQuery(missionId)),
                incidents -> incidents.stream()
                        .map(incident -> IncidentResourceFromEntityAssembler.toResourceFromEntity(missionId, incident))
                        .toList(),
                HttpStatus.OK);
    }
}
