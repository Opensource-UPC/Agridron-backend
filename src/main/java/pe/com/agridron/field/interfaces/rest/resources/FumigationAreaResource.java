package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Response resource for a fumigation area.
 *
 * @param id           fumigation area identifier
 * @param parcelId     owning parcel identifier
 * @param geometry     GeoJSON boundary in WGS 84
 * @param areaHectares geodesic area calculated by the domain
 */
@Schema(name = "FumigationAreaResponse", description = "Fumigation area information")
public record FumigationAreaResource(
        @Schema(description = "Fumigation area identifier", example = "1")
        Long id,

        @Schema(description = "Owning parcel identifier", example = "1")
        Long parcelId,

        @Schema(description = "GeoJSON polygon boundary in WGS 84")
        PolygonResource geometry,

        @Schema(description = "Geodesic area in hectares", example = "0.1234")
        BigDecimal areaHectares) {
}
