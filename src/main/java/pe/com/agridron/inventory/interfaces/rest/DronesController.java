package pe.com.agridron.inventory.interfaces.rest;

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
import pe.com.agridron.inventory.application.commandservices.DroneCommandService;
import pe.com.agridron.inventory.application.queryservices.DroneQueryService;
import pe.com.agridron.inventory.domain.model.queries.GetAllDronesQuery;
import pe.com.agridron.inventory.domain.model.queries.GetDroneByIdQuery;
import pe.com.agridron.inventory.interfaces.rest.resources.CreateDroneResource;
import pe.com.agridron.inventory.interfaces.rest.resources.DroneResource;
import pe.com.agridron.inventory.interfaces.rest.transform.CreateDroneCommandFromResourceAssembler;
import pe.com.agridron.inventory.interfaces.rest.transform.DroneResourceFromEntityAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

/**
 * Endpoints REST de la flota de drones (DR-001, DR-002, TU-020).
 */
@RestController
@RequestMapping("/api/v1/drones")
@Tag(name = "Drones", description = "Drone fleet endpoints")
public class DronesController {

    private final DroneCommandService droneCommandService;
    private final DroneQueryService droneQueryService;

    public DronesController(DroneCommandService droneCommandService, DroneQueryService droneQueryService) {
        this.droneCommandService = droneCommandService;
        this.droneQueryService = droneQueryService;
    }

    @GetMapping
    @Operation(summary = "List drones", description = "Retrieves every drone in the fleet ordered by id.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Drones retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DroneResource.class))))
    })
    public ResponseEntity<List<DroneResource>> getAllDrones() {
        var drones = droneQueryService.handle(new GetAllDronesQuery()).stream()
                .map(DroneResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(drones);
    }

    @GetMapping("/{droneId}")
    @Operation(summary = "Get a drone by ID", description = "Retrieves a drone by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Drone found",
                    content = @Content(schema = @Schema(implementation = DroneResource.class))),
            @ApiResponse(responseCode = "404", description = "Drone not found")
    })
    public ResponseEntity<?> getDroneById(
            @PathVariable
            @Parameter(description = "Drone identifier", example = "1", required = true)
            Long droneId) {
        var result = droneQueryService.handle(new GetDroneByIdQuery(droneId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DroneResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @PostMapping
    @Operation(
            summary = "Register a drone",
            description = "Registers a drone as available with 0 flight hours and installs one unit "
                    + "(good first, otherwise worn) of the given nozzle.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Drone created",
                    content = @Content(schema = @Schema(implementation = DroneResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Nozzle not found"),
            @ApiResponse(responseCode = "409", description = "Serial number already registered"),
            @ApiResponse(responseCode = "422", description = "The nozzle has no installable units")
    })
    public ResponseEntity<?> createDrone(@Valid @RequestBody CreateDroneResource resource) {
        var command = CreateDroneCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = droneCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DroneResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }
}