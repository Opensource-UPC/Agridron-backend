package pe.com.agridron.inventory.interfaces.rest.resources;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JSON de salida de una boquilla. Tiene los mismos campos que usa el frontend.
 */
@Schema(name = "NozzleResponse", description = "Nozzle type and its warehouse stock")
public record NozzleResource(
        @Schema(description = "Nozzle identifier", example = "1")
        Long id,

        @Schema(description = "Nozzle type", example = "flat-fan")
        String type,

        @Schema(description = "Diameter in millimeters", example = "5")
        Double diameterMm,

        @Schema(description = "Units in good condition", example = "6")
        int goodUnits,

        @Schema(description = "Worn units", example = "0")
        int wornUnits,

        @Schema(description = "Damaged units", example = "0")
        int damagedUnits) {
}