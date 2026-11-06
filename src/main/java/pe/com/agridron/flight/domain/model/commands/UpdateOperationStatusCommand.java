package pe.com.agridron.flight.domain.model.commands;

import pe.com.agridron.flight.domain.model.valueobjects.OperationState;

import java.time.Instant;

/**
 * Command that records a telemetry report of the drone flying a mission.
 */
public record UpdateOperationStatusCommand(Long missionId, Double latitude, Double longitude, OperationState state, Instant timestamp) {
}
