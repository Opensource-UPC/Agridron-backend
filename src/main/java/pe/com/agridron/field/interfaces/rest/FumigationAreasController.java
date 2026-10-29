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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.field.application.commandservices.FumigationAreaCommandService;
import pe.com.agridron.field.application.queryservices.FumigationAreaQueryService;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.commands.RedefineFumigationAreaCommand;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreaByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreasByParcelQuery;
import pe.com.agridron.field.interfaces.rest.resources.CreateFumigationAreaResource;
import pe.com.agridron.field.interfaces.rest.resources.FumigationAreaResource;
import pe.com.agridron.field.interfaces.rest.resources.PolygonResource;
import pe.com.agridron.field.interfaces.rest.transform.DelimitFumigationAreaCommandFromResourceAssembler;
import pe.com.agridron.field.interfaces.rest.transform.FieldGeometryFromPolygonResourceAssembler;
import pe.com.agridron.field.interfaces.rest.transform.FumigationAreaResourceFromEntityAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

/**
 * REST controller that exposes fumigation area endpoints.
 */
@RestController
@RequestMapping("/api/v1/fumigation-areas")
@Tag(name = "Fumigation Areas", description = "Fumigation area management endpoints")
public class FumigationAreasController {

    private final FumigationAreaCommandService fumigationAreaCommandService;
    private final FumigationAreaQueryService fumigationAreaQueryService;

    public FumigationAreasController(
            FumigationAreaCommandService fumigationAreaCommandService,
            FumigationAreaQueryService fumigationAreaQueryService) {
        this.fumigationAreaCommandService = fumigationAreaCommandService;
        this.fumigationAreaQueryService = fumigationAreaQueryService;
    }

    /**
     * Delimits a fumigation area inside a parcel.
     */
    @PostMapping
    @Operation(
            summary = "Delimit a fumigation area",
            description = "Creates a fumigation area fully contained in the given parcel. "
                    + "An area outside the parcel is rejected with 422.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Fumigation area created",
                    content = @Content(schema = @Schema(implementation = FumigationAreaResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or invalid geometry"),
            @ApiResponse(responseCode = "404", description = "Parcel not found"),
            @ApiResponse(responseCode = "422", description = "The area is not contained in the parcel")
    })
    public ResponseEntity<?> delimitFumigationArea(
            @Valid @RequestBody CreateFumigationAreaResource resource) {
        var command = DelimitFumigationAreaCommandFromResourceAssembler
                .toCommandFromResource(resource);
        var result = fumigationAreaCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FumigationAreaResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    /**
     * Lists the fumigation areas of a parcel.
     */
    @GetMapping
    @Operation(
            summary = "List fumigation areas by parcel",
            description = "Retrieves every fumigation area delimited inside the given parcel.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fumigation areas retrieved",
                    content = @Content(schema = @Schema(implementation = FumigationAreaResource.class))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid parcelId"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<?> getFumigationAreasByParcel(
            @RequestParam
            @Parameter(description = "Parcel identifier", example = "1", required = true)
            Long parcelId) {
        var result = fumigationAreaQueryService.handle(
                new GetFumigationAreasByParcelQuery(parcelId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FumigationAreasController::toResources,
                HttpStatus.OK);
    }

    /**
     * Retrieves a fumigation area by its identifier.
     */
    @GetMapping("/{fumigationAreaId}")
    @Operation(
            summary = "Get a fumigation area by ID",
            description = "Retrieves a fumigation area's boundary and calculated area.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fumigation area found",
                    content = @Content(schema = @Schema(implementation = FumigationAreaResource.class))),
            @ApiResponse(responseCode = "404", description = "Fumigation area not found")
    })
    public ResponseEntity<?> getFumigationAreaById(
            @PathVariable
            @Parameter(description = "Fumigation area identifier", example = "1", required = true)
            Long fumigationAreaId) {
        var result = fumigationAreaQueryService.handle(
                new GetFumigationAreaByIdQuery(fumigationAreaId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FumigationAreaResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    /**
     * Redefines the boundary of a fumigation area.
     */
    @PutMapping("/{fumigationAreaId}")
    @Operation(
            summary = "Redefine a fumigation area",
            description = "Replaces the boundary with a GeoJSON polygon contained in the parcel.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fumigation area updated",
                    content = @Content(schema = @Schema(implementation = FumigationAreaResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid geometry"),
            @ApiResponse(responseCode = "404", description = "Fumigation area or parcel not found"),
            @ApiResponse(responseCode = "422", description = "The area is not contained in the parcel")
    })
    public ResponseEntity<?> redefineFumigationArea(
            @PathVariable
            @Parameter(description = "Fumigation area identifier", example = "1", required = true)
            Long fumigationAreaId,
            @RequestBody PolygonResource resource) {
        var command = new RedefineFumigationAreaCommand(
                fumigationAreaId,
                FieldGeometryFromPolygonResourceAssembler.toFieldGeometryFromResource(resource));
        var result = fumigationAreaCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                FumigationAreaResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    private static List<FumigationAreaResource> toResources(List<FumigationArea> areas) {
        return areas.stream()
                .map(FumigationAreaResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }
}
