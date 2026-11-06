package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request body for moving a mission to another date.
 *
 * @param requestedDate new flight date
 */
@Schema(name = "RescheduleMissionRequest", description = "New date of a mission")
public record RescheduleMissionResource(
        @Schema(description = "Requested flight date", example = "2026-11-22")
        @NotNull(message = "is required")
        LocalDate requestedDate) {
}
