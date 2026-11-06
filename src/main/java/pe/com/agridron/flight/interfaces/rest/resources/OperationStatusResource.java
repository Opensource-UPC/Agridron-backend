package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.com.agridron.flight.domain.model.valueobjects.OperationState;

import java.time.Instant;

/**
 * Response resource for the last operational status of a mission.
 *
 * @param latitude  WGS 84 latitude
 * @param longitude WGS 84 longitude
 * @param state     flight phase
 * @param timestamp instant of the report, in UTC
 */
@Schema(name = "OperationStatusResponse", description = "Last telemetry report of a mission")
public record OperationStatusResource(
        @Schema(description = "Latitude", example = "-12.2711")
        Double latitude,

        @Schema(description = "Longitude", example = "-76.8705")
        Double longitude,

        @Schema(description = "Flight phase", example = "SPRAYING")
        OperationState state,

        @Schema(description = "Report instant (UTC)", example = "2026-11-20T14:03:00Z")
        Instant timestamp) {
}
