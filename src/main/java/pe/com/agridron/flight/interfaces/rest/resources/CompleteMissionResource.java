package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request body for completing a mission and registering its result.
 *
 * @param treatedArea   treated area, in hectares
 * @param appliedVolume applied agrochemical volume, in liters
 * @param observations  optional free-text observations
 */
@Schema(name = "CompleteMissionRequest", description = "Result of a completed mission")
public record CompleteMissionResource(
        @Schema(description = "Treated area (ha)", example = "4.2")
        @NotNull(message = "is required")
        @PositiveOrZero(message = "must be zero or positive")
        Double treatedArea,

        @Schema(description = "Applied volume (L)", example = "50.4")
        @NotNull(message = "is required")
        @PositiveOrZero(message = "must be zero or positive")
        Double appliedVolume,

        @Schema(description = "Optional observations", example = "Light wind on the north side")
        @Size(max = 1000, message = "must have at most 1000 characters")
        String observations) {
}
