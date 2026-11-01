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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.field.application.queryservices.CropQueryService;
import pe.com.agridron.field.domain.model.queries.GetAllCropsQuery;
import pe.com.agridron.field.domain.model.queries.GetCropByIdQuery;
import pe.com.agridron.field.interfaces.rest.resources.CropResource;
import pe.com.agridron.field.interfaces.rest.transform.CollectionResponseAssembler;
import pe.com.agridron.field.interfaces.rest.transform.CropResourceFromEntityAssembler;
import pe.com.agridron.field.interfaces.rest.transform.PaginationFromRequestAssembler;
import pe.com.agridron.field.interfaces.rest.transform.SortOrderFromRequestAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.Set;

/**
 * REST controller that exposes the read-only crop catalog.
 */
@RestController
@RequestMapping("/api/v1/crops")
@Tag(name = "Crops", description = "Crop catalog endpoints")
public class CropsController {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "variety");

    private final CropQueryService cropQueryService;

    public CropsController(CropQueryService cropQueryService) {
        this.cropQueryService = cropQueryService;
    }

    /**
     * Lists the crops of the catalog.
     */
    @GetMapping
    @Operation(
            summary = "List crops",
            description = "Retrieves the crops available in the read-only catalog. Without page "
                    + "or size it returns every crop; with page or size it returns a paged "
                    + "envelope.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Crops retrieved",
                    content = @Content(schema = @Schema(implementation = CropResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<?> getAllCrops(
            @RequestParam(required = false)
            @Parameter(description = "Zero-based page index", example = "0")
            Integer page,
            @RequestParam(required = false)
            @Parameter(description = "Page size (1-100)", example = "20")
            Integer size,
            @RequestParam(required = false)
            @Parameter(description = "Order as 'field' or 'field,asc|desc'", example = "name,asc")
            String sort) {
        var pagination = PaginationFromRequestAssembler.toPaginationFromRequest(page, size);
        var sortOrder = SortOrderFromRequestAssembler.toSortOrderFromRequest(sort, SORTABLE_FIELDS);
        var result = cropQueryService.handle(new GetAllCropsQuery(pagination, sortOrder));
        return CollectionResponseAssembler.toResponseEntityFromPageResult(
                result,
                pagination.paged(),
                CropResourceFromEntityAssembler::toResourceFromEntity);
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
