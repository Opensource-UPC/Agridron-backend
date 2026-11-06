package pe.com.agridron.inventory.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * JSON de entrada para registrar un tipo de boquilla con su stock inicial.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "CreateNozzleRequest", description = "Data required to register a nozzle type")
public record CreateNozzleResource(
        @Schema(description = "Nozzle type", example = "flat-fan")
        @NotBlank(message = "is required")
        @Size(max = 60, message = "must have at most 60 characters")
        String type,

        @Schema(description = "Diameter in millimeters", example = "5")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Double diameterMm,

        @Schema(description = "Units in good condition", example = "6")
        @NotNull(message = "is required")
        @PositiveOrZero(message = "must not be negative")
        Integer goodUnits,

        @Schema(description = "Worn units", example = "0")
        @NotNull(message = "is required")
        @PositiveOrZero(message = "must not be negative")
        Integer wornUnits,

        @Schema(description = "Damaged units", example = "0")
        @NotNull(message = "is required")
        @PositiveOrZero(message = "must not be negative")
        Integer damagedUnits) {
}