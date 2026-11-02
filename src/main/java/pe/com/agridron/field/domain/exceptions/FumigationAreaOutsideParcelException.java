package pe.com.agridron.field.domain.exceptions;

/**
 * Thrown when a fumigation area is not fully contained in its parcel or exceeds its area.
 */
public class FumigationAreaOutsideParcelException extends RuntimeException {

    public FumigationAreaOutsideParcelException(String message) {
        super(message);
    }
}
