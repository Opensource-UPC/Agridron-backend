package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for assigning the drone and the agrochemical of a planned mission.
 *
 * @param droneId        drone from the Inventory bounded context
 * @param agrochemicalId agrochemical supply from the Inventory bounded context
 */
@Schema(name = "AssignMissionResourcesRequest", description = "Drone and agrochemical of a mission")
public record AssignMissionResourcesResource(
        @Schema(description = "Drone identifier", example = "3")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long droneId,

        @Schema(description = "Agrochemical identifier", example = "5")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long agrochemicalId) {
}
