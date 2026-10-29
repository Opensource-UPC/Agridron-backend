package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;

import java.math.BigDecimal;

/**
 * Response resource for a parcel.
 *
 * @param id                parcel identifier
 * @param farmId            owning farm identifier
 * @param cropId            optional crop identifier
 * @param name              parcel name
 * @param geometry          GeoJSON boundary in WGS 84
 * @param areaHectares      geodesic area calculated by the domain
 * @param status            operational status
 * @param phenologicalStage phenological stage, present only when a crop is assigned
 */
@Schema(name = "ParcelResponse", description = "Parcel information")
public record ParcelResource(
        @Schema(description = "Parcel identifier", example = "1")
        Long id,

        @Schema(description = "Owning farm identifier", example = "1")
        Long farmId,

        @Schema(description = "Crop identifier", example = "2", nullable = true)
        Long cropId,

        @Schema(description = "Parcel name", example = "Lote 1")
        String name,

        @Schema(description = "GeoJSON polygon boundary in WGS 84")
        PolygonResource geometry,

        @Schema(description = "Geodesic area in hectares", example = "1.2345")
        BigDecimal areaHectares,

        @Schema(description = "Operational status", example = "ACTIVE")
        ParcelStatus status,

        @Schema(description = "Phenological stage", example = "VEGETATIVE", nullable = true)
        PhenologicalStage phenologicalStage) {
}
