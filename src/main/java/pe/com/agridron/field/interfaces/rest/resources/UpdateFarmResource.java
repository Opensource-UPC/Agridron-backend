package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for updating the descriptive data of a farm.
 *
 * @param name       new farm name, unique per owner
 * @param location   new human-readable location
 * @param department new Peruvian department
 */
@Schema(name = "UpdateFarmRequest", description = "Data required to update a farm")
public record UpdateFarmResource(
        @Schema(description = "Farm name", example = "Finca El Sol")
        @NotBlank(message = "is required")
        @Size(max = 120, message = "must have at most 120 characters")
        String name,

        @Schema(description = "Human-readable location", example = "Lurín, Lima")
        @NotBlank(message = "is required")
        @Size(max = 255, message = "must have at most 255 characters")
        String location,

        @Schema(description = "Peruvian department", example = "Lima")
        @NotBlank(message = "is required")
        @Size(max = 100, message = "must have at most 100 characters")
        String department) {
}
