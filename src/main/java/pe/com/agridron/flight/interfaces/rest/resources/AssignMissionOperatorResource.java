package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for assigning the operator of a planned mission.
 *
 * @param operatorId operator, a user of the IAM bounded context
 */
@Schema(name = "AssignMissionOperatorRequest", description = "Operator of a mission")
public record AssignMissionOperatorResource(
        @Schema(description = "Operator identifier", example = "20")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long operatorId) {
}
