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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.field.application.commandservices.ParcelCommandService;
import pe.com.agridron.field.application.queryservices.ParcelQueryService;
import pe.com.agridron.field.domain.model.commands.ChangeParcelStatusCommand;
import pe.com.agridron.field.domain.model.commands.RedefineParcelGeometryCommand;
import pe.com.agridron.field.domain.model.queries.GetParcelByIdQuery;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.interfaces.rest.resources.ChangeParcelStatusResource;
import pe.com.agridron.field.interfaces.rest.resources.CreateParcelResource;
import pe.com.agridron.field.interfaces.rest.resources.ParcelResource;
import pe.com.agridron.field.interfaces.rest.resources.PolygonResource;
import pe.com.agridron.field.interfaces.rest.resources.UpdateParcelResource;
import pe.com.agridron.field.interfaces.rest.transform.CreateParcelCommandFromResourceAssembler;
import pe.com.agridron.field.interfaces.rest.transform.FieldGeometryFromPolygonResourceAssembler;
import pe.com.agridron.field.interfaces.rest.transform.ParcelResourceFromEntityAssembler;
import pe.com.agridron.field.interfaces.rest.transform.UpdateParcelCommandFromResourceAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller that exposes parcel management endpoints.
 */
@RestController
@RequestMapping("/api/v1/parcels")
@Tag(name = "Parcels", description = "Parcel management endpoints")
public class ParcelsController {

    private final ParcelCommandService parcelCommandService;
    private final ParcelQueryService parcelQueryService;

    public ParcelsController(
            ParcelCommandService parcelCommandService,
            ParcelQueryService parcelQueryService) {
        this.parcelCommandService = parcelCommandService;
        this.parcelQueryService = parcelQueryService;
    }

    /**
     * Creates a parcel inside an existing farm.
     */
    @PostMapping
    @Operation(
            summary = "Create a parcel",
            description = "Creates a parcel with a GeoJSON boundary in an existing farm.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Parcel created",
                    content = @Content(schema = @Schema(implementation = ParcelResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or invalid geometry"),
            @ApiResponse(responseCode = "404", description = "Farm or crop not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - duplicated parcel name in the farm")
    })
    public ResponseEntity<?> createParcel(@Valid @RequestBody CreateParcelResource resource) {
        var command = CreateParcelCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = parcelCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ParcelResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    /**
     * Searches parcels by the given optional filters.
     */
    @GetMapping
    @Operation(
            summary = "Search parcels",
            description = "Searches parcels combining the supplied filters with AND. "
                    + "Every filter is optional; without filters every parcel is returned.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Parcels retrieved",
                    content = @Content(schema = @Schema(implementation = ParcelResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter combination")
    })
    public ResponseEntity<List<ParcelResource>> searchParcels(
            @RequestParam(required = false)
            @Parameter(description = "Farm identifier", example = "1")
            Long farmId,
            @RequestParam(required = false)
            @Parameter(description = "Operational status", example = "ACTIVE")
            ParcelStatus status,
            @RequestParam(required = false)
            @Parameter(description = "Crop identifier", example = "2")
            Long cropId,
            @RequestParam(required = false)
            @Parameter(description = "Inclusive minimum area in hectares", example = "0.5")
            BigDecimal minAreaHectares,
            @RequestParam(required = false)
            @Parameter(description = "Inclusive maximum area in hectares", example = "10")
            BigDecimal maxAreaHectares) {
        var criteria = new ParcelSearchCriteria(
                farmId, status, cropId, minAreaHectares, maxAreaHectares);
        var resources = parcelQueryService.handle(criteria).stream()
                .map(ParcelResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a parcel by its identifier.
     */
    @GetMapping("/{parcelId}")
    @Operation(
            summary = "Get a parcel by ID",
            description = "Retrieves a parcel's boundary, area, status and crop information.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Parcel found",
                    content = @Content(schema = @Schema(implementation = ParcelResource.class))),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<?> getParcelById(
            @PathVariable
            @Parameter(description = "Parcel identifier", example = "1", required = true)
            Long parcelId) {
        var result = parcelQueryService.handle(new GetParcelByIdQuery(parcelId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ParcelResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    /**
     * Updates the editable data of a parcel.
     */
    @PutMapping("/{parcelId}")
    @Operation(
            summary = "Update a parcel",
            description = "Replaces the name and crop information of a parcel.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Parcel updated",
                    content = @Content(schema = @Schema(implementation = ParcelResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Parcel or crop not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - duplicated parcel name")
    })
    public ResponseEntity<?> updateParcel(
            @PathVariable
            @Parameter(description = "Parcel identifier", example = "1", required = true)
            Long parcelId,
            @Valid @RequestBody UpdateParcelResource resource) {
        var command = UpdateParcelCommandFromResourceAssembler.toCommandFromResource(parcelId, resource);
        var result = parcelCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ParcelResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    /**
     * Redefines the boundary of a parcel.
     */
    @PutMapping("/{parcelId}/geometry")
    @Operation(
            summary = "Redefine a parcel boundary",
            description = "Replaces the boundary with a GeoJSON polygon and recalculates its area.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Boundary updated",
                    content = @Content(schema = @Schema(implementation = ParcelResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid geometry"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<?> redefineParcelGeometry(
            @PathVariable
            @Parameter(description = "Parcel identifier", example = "1", required = true)
            Long parcelId,
            @RequestBody PolygonResource resource) {
        var geometry = FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource);
        var result = parcelCommandService.handle(
                new RedefineParcelGeometryCommand(parcelId, geometry));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ParcelResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    /**
     * Changes the operational status of a parcel.
     */
    @PatchMapping("/{parcelId}/status")
    @Operation(
            summary = "Change a parcel status",
            description = "Activates or deactivates a parcel. Repeated transitions are idempotent.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated",
                    content = @Content(schema = @Schema(implementation = ParcelResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    public ResponseEntity<?> changeParcelStatus(
            @PathVariable
            @Parameter(description = "Parcel identifier", example = "1", required = true)
            Long parcelId,
            @Valid @RequestBody ChangeParcelStatusResource resource) {
        var result = parcelCommandService.handle(
                new ChangeParcelStatusCommand(parcelId, resource.status()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ParcelResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}
