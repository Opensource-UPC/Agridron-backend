package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;

/**
 * Request body for creating a parcel.
 *
 * @param farmId            identifier of the owning farm
 * @param name              parcel name, unique inside the farm
 * @param geometry          GeoJSON boundary in WGS 84
 * @param cropId            optional crop from the read-only catalog
 * @param phenologicalStage required when {@code cropId} is present
 */
@Schema(name = "CreateParcelRequest", description = "Data required to create a parcel")
public record CreateParcelResource(
        @Schema(description = "Owning farm identifier", example = "1")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long farmId,

        @Schema(description = "Parcel name", example = "Lote 1")
        @NotBlank(message = "is required")
        @Size(max = 120, message = "must have at most 120 characters")
        String name,

        @Schema(description = "GeoJSON polygon boundary in WGS 84", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "is required")
        PolygonResource geometry,

        @Schema(description = "Optional crop identifier", example = "2")
        @Positive(message = "must be a positive number")
        Long cropId,

        @Schema(description = "Phenological stage; required when cropId is present", example = "VEGETATIVE")
        PhenologicalStage phenologicalStage) {
}
