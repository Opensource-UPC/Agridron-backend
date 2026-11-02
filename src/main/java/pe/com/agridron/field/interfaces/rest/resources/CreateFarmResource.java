package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request body for registering a farm.
 *
 * @param ownerId    identifier of the owner in the IAM bounded context
 * @param name       farm name, unique per owner
 * @param location   human-readable location
 * @param department Peruvian department where the farm is located
 */
@Schema(name = "CreateFarmRequest", description = "Data required to register a farm")
public record CreateFarmResource(
        @Schema(description = "Owner identifier", example = "10")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long ownerId,

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
