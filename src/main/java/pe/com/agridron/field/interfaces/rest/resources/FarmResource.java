package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response resource for a farm.
 *
 * @param id            farm identifier
 * @param ownerId       owner identifier in the IAM bounded context
 * @param name          farm name
 * @param location      human-readable location
 * @param department    Peruvian department
 * @param supervisorIds identifiers of the assigned supervisors
 */
@Schema(name = "FarmResponse", description = "Farm information")
public record FarmResource(
        @Schema(description = "Farm identifier", example = "1")
        Long id,

        @Schema(description = "Owner identifier", example = "10")
        Long ownerId,

        @Schema(description = "Farm name", example = "Finca El Sol")
        String name,

        @Schema(description = "Human-readable location", example = "Lurín, Lima")
        String location,

        @Schema(description = "Peruvian department", example = "Lima")
        String department,

        @Schema(description = "Identifiers of the assigned supervisors", example = "[20, 21]")
        List<Long> supervisorIds) {
}
