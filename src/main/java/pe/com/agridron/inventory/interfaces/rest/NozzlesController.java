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
import pe.com.agridron.inventory.application.commandservices.NozzleCommandService;
import pe.com.agridron.inventory.application.queryservices.NozzleQueryService;
import pe.com.agridron.inventory.domain.model.queries.GetAllNozzlesQuery;
import pe.com.agridron.inventory.domain.model.queries.GetNozzleByIdQuery;
import pe.com.agridron.inventory.interfaces.rest.resources.CreateNozzleResource;
import pe.com.agridron.inventory.interfaces.rest.resources.NozzleResource;
import pe.com.agridron.inventory.interfaces.rest.transform.CreateNozzleCommandFromResourceAssembler;
import pe.com.agridron.inventory.interfaces.rest.transform.NozzleResourceFromEntityAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

/**
 * Endpoints REST del catálogo de boquillas (DR-004, TU-021).
 */
@RestController
@RequestMapping("/api/v1/nozzles")
@Tag(name = "Nozzles", description = "Nozzle catalog and warehouse stock endpoints")
public class NozzlesController {

    private final NozzleCommandService nozzleCommandService;
    private final NozzleQueryService nozzleQueryService;

    public NozzlesController(NozzleCommandService nozzleCommandService, NozzleQueryService nozzleQueryService) {
        this.nozzleCommandService = nozzleCommandService;
        this.nozzleQueryService = nozzleQueryService;
    }

    @GetMapping
    @Operation(summary = "List nozzles", description = "Retrieves every nozzle type with its stock, ordered by id.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Nozzles retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NozzleResource.class))))
    })
    public ResponseEntity<List<NozzleResource>> getAllNozzles() {
        var nozzles = nozzleQueryService.handle(new GetAllNozzlesQuery()).stream()
                .map(NozzleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(nozzles);
    }

    @GetMapping("/{nozzleId}")
    @Operation(summary = "Get a nozzle by ID", description = "Retrieves a nozzle type by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Nozzle found",
                    content = @Content(schema = @Schema(implementation = NozzleResource.class))),
            @ApiResponse(responseCode = "404", description = "Nozzle not found")
    })
    public ResponseEntity<?> getNozzleById(
            @PathVariable
            @Parameter(description = "Nozzle identifier", example = "1", required = true)
            Long nozzleId) {
        var result = nozzleQueryService.handle(new GetNozzleByIdQuery(nozzleId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                NozzleResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Register a nozzle type", description = "Registers a nozzle type with its initial stock.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Nozzle created",
                    content = @Content(schema = @Schema(implementation = NozzleResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<?> createNozzle(@Valid @RequestBody CreateNozzleResource resource) {
        var command = CreateNozzleCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = nozzleCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                NozzleResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }
}