package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for delimiting a fumigation area inside a parcel.
 *
 * @param parcelId identifier of the owning parcel
 * @param geometry GeoJSON boundary of the area in WGS 84
 */
@Schema(name = "CreateFumigationAreaRequest", description = "Data required to delimit a fumigation area")
public record CreateFumigationAreaResource(
        @Schema(description = "Owning parcel identifier", example = "1")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long parcelId,

        @Schema(description = "GeoJSON polygon boundary in WGS 84", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "is required")
        PolygonResource geometry) {
}
