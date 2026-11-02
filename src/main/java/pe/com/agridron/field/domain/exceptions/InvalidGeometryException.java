package pe.com.agridron.field.domain.exceptions;

/**
 * Thrown when a geometry does not satisfy the Field Management invariants.
 */
public class InvalidGeometryException extends RuntimeException {

    public InvalidGeometryException(String message) {
        super(message);
    }

    public InvalidGeometryException(String message, Throwable cause) {
        super(message, cause);
    }
}
