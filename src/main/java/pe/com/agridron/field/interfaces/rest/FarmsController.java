package pe.com.agridron.field.interfaces.rest;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.field.application.commandservices.FarmCommandService;
import pe.com.agridron.field.application.queryservices.FarmQueryService;
import pe.com.agridron.field.domain.model.commands.AssignFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.RemoveFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.queries.GetFarmByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFarmsByOwnerQuery;
import pe.com.agridron.field.interfaces.rest.resources.AssignFarmSupervisorResource;
import pe.com.agridron.field.interfaces.rest.resources.CreateFarmResource;
import pe.com.agridron.field.interfaces.rest.resources.FarmResource;
import pe.com.agridron.field.interfaces.rest.resources.UpdateFarmResource;
import pe.com.agridron.field.interfaces.rest.transform.CreateFarmCommandFromResourceAssembler;
import pe.com.agridron.field.interfaces.rest.transform.FarmResourceFromEntityAssembler;
import pe.com.agridron.field.interfaces.rest.transform.UpdateFarmCommandFromResourceAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

/**
 * REST controller that exposes farm management endpoints.
 */
@RestController
@RequestMapping("/api/v1/farms")
@Tag(name = "Farms", description = "Farm management endpoints")
public class FarmsController {

    private final FarmCommandService farmCommandService;
    private final FarmQueryService farmQueryService;

    public FarmsController(FarmCommandService farmCommandService, FarmQueryService farmQueryService) {
        this.farmCommandService = farmCommandService;
        this.farmQueryService = farmQueryService;
    }

    /**
     * Registers a new farm for an owner.
     */
    @PostMapping
    @Operation(
            summary = "Create a farm",
            description = "Registers a new farm owned by the given owner.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Farm created",
                    content = @Content(schema = @Schema(implementation = FarmResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Conflict - the owner already has a farm with that name")
    })
    public ResponseEntity<?> createFarm(@Valid @RequestBody CreateFarmResource resource) {
        var command = CreateFarmCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = farmCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FarmResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    /**
     * Lists the farms owned by a user.
     */
    @GetMapping
    @Operation(
            summary = "List farms by owner",
            description = "Retrieves every farm owned by the given owner, ordered by name.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Farms retrieved",
                    content = @Content(schema = @Schema(implementation = FarmResource.class))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid ownerId")
    })
    public ResponseEntity<List<FarmResource>> getFarmsByOwner(
            @RequestParam
            @Parameter(description = "Owner identifier", example = "10", required = true)
            Long ownerId) {
        var farms = farmQueryService.handle(new GetFarmsByOwnerQuery(ownerId));
        var resources = farms.stream()
                .map(FarmResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a farm by its identifier.
     */
    @GetMapping("/{farmId}")
    @Operation(
            summary = "Get a farm by ID",
            description = "Retrieves a farm's descriptive data and assigned supervisors.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Farm found",
                    content = @Content(schema = @Schema(implementation = FarmResource.class))),
            @ApiResponse(responseCode = "404", description = "Farm not found")
    })
    public ResponseEntity<?> getFarmById(
            @PathVariable
            @Parameter(description = "Farm identifier", example = "1", required = true)
            Long farmId) {
        var result = farmQueryService.handle(new GetFarmByIdQuery(farmId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FarmResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    /**
     * Updates the descriptive data of a farm.
     */
    @PutMapping("/{farmId}")
    @Operation(
            summary = "Update a farm",
            description = "Replaces the name, location and department of an existing farm.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Farm updated",
                    content = @Content(schema = @Schema(implementation = FarmResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Farm not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - duplicated farm name")
    })
    public ResponseEntity<?> updateFarm(
            @PathVariable
            @Parameter(description = "Farm identifier", example = "1", required = true)
            Long farmId,
            @Valid @RequestBody UpdateFarmResource resource) {
        var command = UpdateFarmCommandFromResourceAssembler.toCommandFromResource(farmId, resource);
        var result = farmCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FarmResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    /**
     * Assigns a supervisor to a farm.
     */
    @PostMapping("/{farmId}/supervisors")
    @Operation(
            summary = "Assign a supervisor",
            description = "Assigns a supervisor to a farm. Assigning it twice is idempotent.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Supervisor assigned",
                    content = @Content(schema = @Schema(implementation = FarmResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Farm not found")
    })
    public ResponseEntity<?> assignSupervisor(
            @PathVariable
            @Parameter(description = "Farm identifier", example = "1", required = true)
            Long farmId,
            @Valid @RequestBody AssignFarmSupervisorResource resource) {
        var result = farmCommandService.handle(
                new AssignFarmSupervisorCommand(farmId, resource.supervisorId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FarmResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    /**
     * Removes a supervisor from a farm.
     */
    @DeleteMapping("/{farmId}/supervisors/{supervisorId}")
    @Operation(
            summary = "Remove a supervisor",
            description = "Removes a supervisor from a farm. Removing an absent one is idempotent.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Supervisor removed",
                    content = @Content(schema = @Schema(implementation = FarmResource.class))),
            @ApiResponse(responseCode = "404", description = "Farm not found")
    })
    public ResponseEntity<?> removeSupervisor(
            @PathVariable
            @Parameter(description = "Farm identifier", example = "1", required = true)
            Long farmId,
            @PathVariable
            @Parameter(description = "Supervisor identifier", example = "20", required = true)
            Long supervisorId) {
        var result = farmCommandService.handle(
                new RemoveFarmSupervisorCommand(farmId, supervisorId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FarmResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}
