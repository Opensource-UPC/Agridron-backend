package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response resource for the result of a completed mission.
 *
 * @param id            result identifier
 * @param treatedArea   treated area, in hectares
 * @param appliedVolume applied agrochemical volume, in liters
 * @param observations  optional observations
 */
@Schema(name = "MissionResultResponse", description = "Result of a completed mission")
public record MissionResultResource(
        @Schema(description = "Result identifier", example = "1")
        Long id,

        @Schema(description = "Treated area (ha)", example = "4.2")
        Double treatedArea,

        @Schema(description = "Applied volume (L)", example = "50.4")
        Double appliedVolume,

        @Schema(description = "Observations", nullable = true)
        String observations) {
}
