package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import pe.com.agridron.flight.domain.model.valueobjects.OperationState;

import java.time.Instant;

/**
 * Request body with a telemetry report of the drone flying a mission.
 *
 * @param latitude  WGS 84 latitude
 * @param longitude WGS 84 longitude
 * @param state     current flight phase
 * @param timestamp instant of the report, in UTC
 */
@Schema(name = "UpdateOperationStatusRequest", description = "Telemetry report of a mission")
public record UpdateOperationStatusResource(
        @Schema(description = "Latitude", example = "-12.2711")
        @NotNull(message = "is required")
        @DecimalMin(value = "-90", message = "must be greater than or equal to -90")
        @DecimalMax(value = "90", message = "must be less than or equal to 90")
        Double latitude,

        @Schema(description = "Longitude", example = "-76.8705")
        @NotNull(message = "is required")
        @DecimalMin(value = "-180", message = "must be greater than or equal to -180")
        @DecimalMax(value = "180", message = "must be less than or equal to 180")
        Double longitude,

        @Schema(description = "Flight phase", example = "SPRAYING")
        @NotNull(message = "is required")
        OperationState state,

        @Schema(description = "Report instant (UTC)", example = "2026-11-20T14:03:00Z")
        @NotNull(message = "is required")
        Instant timestamp) {
}
