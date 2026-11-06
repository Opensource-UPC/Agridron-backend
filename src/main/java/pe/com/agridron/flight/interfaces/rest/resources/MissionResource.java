package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;

import java.time.LocalDate;

/**
 * Response resource for a mission.
 *
 * @param id               mission identifier
 * @param parcelId         sprayed parcel
 * @param requestedDate    requested flight date
 * @param droneId          assigned drone, {@code null} until resources are assigned
 * @param agrochemicalId   assigned agrochemical, {@code null} until resources are assigned
 * @param operatorId       assigned operator, {@code null} until assigned
 * @param flightParameters flight configuration, {@code null} until configured
 * @param status           lifecycle status
 * @param result           result, present only when the mission is completed
 * @param operationStatus  last telemetry report, {@code null} until the first one arrives
 * @param incidentCount    number of reported incidents
 */
@Schema(name = "MissionResponse", description = "Mission information")
public record MissionResource(
        @Schema(description = "Mission identifier", example = "1")
        Long id,

        @Schema(description = "Parcel identifier", example = "1")
        Long parcelId,

        @Schema(description = "Requested flight date", example = "2026-11-20")
        LocalDate requestedDate,

        @Schema(description = "Drone identifier", example = "3", nullable = true)
        Long droneId,

        @Schema(description = "Agrochemical identifier", example = "5", nullable = true)
        Long agrochemicalId,

        @Schema(description = "Operator identifier", example = "20", nullable = true)
        Long operatorId,

        @Schema(description = "Flight parameters", nullable = true)
        FlightParametersResource flightParameters,

        @Schema(description = "Lifecycle status", example = "PLANNED")
        MissionStatus status,

        @Schema(description = "Mission result", nullable = true)
        MissionResultResource result,

        @Schema(description = "Last operational status", nullable = true)
        OperationStatusResource operationStatus,

        @Schema(description = "Number of reported incidents", example = "0")
        int incidentCount) {
}
