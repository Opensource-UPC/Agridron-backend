package pe.com.agridron.flight.domain.model.commands;

import java.time.Instant;

/**
 * Command that reports an incident during a mission in progress or paused.
 */
public record ReportIncidentCommand(Long missionId, String type, String description, Instant occurredAt) {
}
