package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for assigning a supervisor to a farm.
 *
 * @param supervisorId identifier of the supervisor in the IAM bounded context
 */
@Schema(name = "AssignFarmSupervisorRequest", description = "Supervisor to assign to a farm")
public record AssignFarmSupervisorResource(
        @Schema(description = "Supervisor identifier", example = "20")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long supervisorId) {
}
