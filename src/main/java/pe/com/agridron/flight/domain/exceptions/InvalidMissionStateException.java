package pe.com.agridron.flight.domain.exceptions;

/**
 * Thrown when a mission operation is not allowed in the mission's current status.
 *
 * <p>Command services translate it into {@code MISSION_INVALID_STATE} (HTTP 422).</p>
 */
public class InvalidMissionStateException extends RuntimeException {

    public InvalidMissionStateException(String message) {
        super(message);
    }
}
