package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Request body for planning a mission.
 *
 * @param parcelId      parcel to be sprayed, from the Field Management bounded context
 * @param requestedDate date on which the mission should be flown
 */
@Schema(name = "PlanMissionRequest", description = "Data required to plan a mission")
public record PlanMissionResource(
        @Schema(description = "Parcel identifier", example = "1")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long parcelId,

        @Schema(description = "Requested flight date", example = "2026-11-20")
        @NotNull(message = "is required")
        LocalDate requestedDate) {
}
