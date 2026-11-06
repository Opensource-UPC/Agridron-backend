package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Request body for reporting an incident during a mission.
 *
 * @param type        incident category
 * @param description what happened
 * @param occurredAt  when it happened, in UTC
 */
@Schema(name = "ReportIncidentRequest", description = "Incident reported during a mission")
public record ReportIncidentResource(
        @Schema(description = "Incident category", example = "LOW_BATTERY")
        @NotBlank(message = "is required")
        @Size(max = 50, message = "must have at most 50 characters")
        String type,

        @Schema(description = "Incident description", example = "Battery dropped below 20%")
        @NotBlank(message = "is required")
        @Size(max = 1000, message = "must have at most 1000 characters")
        String description,

        @Schema(description = "Occurrence instant (UTC)", example = "2026-11-20T14:05:00Z")
        @NotNull(message = "is required")
        Instant occurredAt) {
}
