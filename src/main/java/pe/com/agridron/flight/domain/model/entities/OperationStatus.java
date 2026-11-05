package pe.com.agridron.flight.domain.model.entities;

import lombok.Getter;
import pe.com.agridron.flight.domain.model.valueobjects.FlightCoordinates;
import pe.com.agridron.flight.domain.model.valueobjects.OperationState;

import java.time.Instant;
import java.util.Objects;

/**
 * Last known operational status of the drone flying a mission.
 *
 * <p>It is part of the {@code Mission} aggregate. The first telemetry report registers it and
 * later reports update it in place. Reports older than the current one are ignored, so
 * out-of-order delivery never moves the status backwards.</p>
 */
@Getter
public class OperationStatus {

    private Long id;
    private FlightCoordinates flightCoordinates;
    private OperationState status;
    private Instant timestamp;

    private OperationStatus(FlightCoordinates flightCoordinates, OperationState status, Instant timestamp) {
        this.flightCoordinates = Objects.requireNonNull(flightCoordinates, "flightCoordinates is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp is required");
    }

    /**
     * Registers the first operational status of a mission.
     */
    public static OperationStatus register(
            FlightCoordinates flightCoordinates, OperationState status, Instant timestamp) {
        return new OperationStatus(flightCoordinates, status, timestamp);
    }

    /**
     * Reconstitutes an existing operational status from persistence state.
     */
    public static OperationStatus reconstitute(
            Long id, FlightCoordinates flightCoordinates, OperationState status, Instant timestamp) {
        var operationStatus = new OperationStatus(flightCoordinates, status, timestamp);
        operationStatus.id = id;
        return operationStatus;
    }

    /**
     * Updates the drone location.
     *
     * @return {@code true} when applied, {@code false} when the report is older than the current one
     */
    public boolean updateLocation(FlightCoordinates coordinates, Instant timestamp) {
        Objects.requireNonNull(coordinates, "flightCoordinates is required");
        if (isStale(timestamp)) {
            return false;
        }
        this.flightCoordinates = coordinates;
        this.timestamp = timestamp;
        return true;
    }

    /**
     * Updates the flight phase.
     *
     * @return {@code true} when applied, {@code false} when the report is older than the current one
     */
    public boolean updateStatus(OperationState status, Instant timestamp) {
        Objects.requireNonNull(status, "status is required");
        if (isStale(timestamp)) {
            return false;
        }
        this.status = status;
        this.timestamp = timestamp;
        return true;
    }

    private boolean isStale(Instant timestamp) {
        Objects.requireNonNull(timestamp, "timestamp is required");
        return timestamp.isBefore(this.timestamp);
    }
}
