package pe.com.agridron.flight.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response resource for an incident.
 *
 * @param id          incident identifier
 * @param missionId   owning mission
 * @param type        incident category
 * @param description what happened
 * @param occurredAt  when it happened, in UTC
 */
@Schema(name = "IncidentResponse", description = "Incident reported during a mission")
public record IncidentResource(
        @Schema(description = "Incident identifier", example = "1")
        Long id,

        @Schema(description = "Mission identifier", example = "1")
        Long missionId,

        @Schema(description = "Incident category", example = "LOW_BATTERY")
        String type,

        @Schema(description = "Incident description", example = "Battery dropped below 20%")
        String description,

        @Schema(description = "Occurrence instant (UTC)", example = "2026-11-20T14:05:00Z")
        Instant occurredAt) {
}
