package pe.com.agridron.field.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.field.application.queryservices.CropQueryService;
import pe.com.agridron.field.domain.model.queries.GetAllCropsQuery;
import pe.com.agridron.field.domain.model.queries.GetCropByIdQuery;
import pe.com.agridron.field.interfaces.rest.resources.CropResource;
import pe.com.agridron.field.interfaces.rest.transform.CropResourceFromEntityAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

/**
 * REST controller that exposes the read-only crop catalog.
 */
@RestController
@RequestMapping("/api/v1/crops")
@Tag(name = "Crops", description = "Crop catalog endpoints")
public class CropsController {

    private final CropQueryService cropQueryService;

    public CropsController(CropQueryService cropQueryService) {
        this.cropQueryService = cropQueryService;
    }

    /**
     * Lists every crop of the catalog.
     */
    @GetMapping
    @Operation(
            summary = "List crops",
            description = "Retrieves every crop available in the read-only catalog.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Crops retrieved",
                    content = @Content(schema = @Schema(implementation = CropResource.class)))
    })
    public ResponseEntity<List<CropResource>> getAllCrops() {
        var resources = cropQueryService.handle(new GetAllCropsQuery()).stream()
                .map(CropResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a crop by its identifier.
     */
    @GetMapping("/{cropId}")
    @Operation(
            summary = "Get a crop by ID",
            description = "Retrieves a crop catalog entry by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Crop found",
                    content = @Content(schema = @Schema(implementation = CropResource.class))),
            @ApiResponse(responseCode = "404", description = "Crop not found")
    })
    public ResponseEntity<?> getCropById(
            @PathVariable
            @Parameter(description = "Crop identifier", example = "1", required = true)
            Long cropId) {
        var result = cropQueryService.handle(new GetCropByIdQuery(cropId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CropResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}
