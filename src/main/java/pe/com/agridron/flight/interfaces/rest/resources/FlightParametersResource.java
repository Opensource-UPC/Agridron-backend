package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Flight parameters of a mission, used both as request body and inside responses.
 *
 * @param flightHeight height above the canopy, in meters
 * @param flightSpeed  ground speed, in meters per second
 * @param sprayRate    application rate, in liters per hectare
 * @param overlap      lateral overlap between passes, as a percentage in {@code [0, 100)}
 */
@Schema(name = "FlightParameters", description = "Flight configuration of a mission")
public record FlightParametersResource(
        @Schema(description = "Height above the canopy (m)", example = "3.5")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Double flightHeight,

        @Schema(description = "Ground speed (m/s)", example = "5.0")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Double flightSpeed,

        @Schema(description = "Application rate (L/ha)", example = "12.0")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Double sprayRate,

        @Schema(description = "Lateral overlap (%)", example = "20.0")
        @NotNull(message = "is required")
        @PositiveOrZero(message = "must be zero or positive")
        @DecimalMax(value = "100", inclusive = false, message = "must be lower than 100")
        Double overlap) {
}
