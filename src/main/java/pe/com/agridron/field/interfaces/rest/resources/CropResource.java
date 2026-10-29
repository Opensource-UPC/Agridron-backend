package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response resource for a crop catalog entry.
 *
 * @param id      crop identifier
 * @param name    crop name
 * @param variety crop variety
 */
@Schema(name = "CropResponse", description = "Crop catalog entry")
public record CropResource(
        @Schema(description = "Crop identifier", example = "1")
        Long id,

        @Schema(description = "Crop name", example = "Palta")
        String name,

        @Schema(description = "Crop variety", example = "Hass")
        String variety) {
}
