package pe.com.agridron.field.interfaces.rest;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.com.agridron.field.domain.exceptions.InvalidGeometryException;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;

/**
 * REST advice for Field Management controllers.
 *
 * <p>Translates the domain {@link InvalidGeometryException} into an RFC 9457 response with
 * the {@code INVALID_GEOMETRY} code and HTTP 400. It runs before the global handler,
 * which would otherwise treat the exception as an unexpected failure.</p>
 */
@RestControllerAdvice(basePackages = "pe.com.agridron.field.interfaces.rest")
@Order(Ordered.HIGHEST_PRECEDENCE)
@NullMarked
public class FieldExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(FieldExceptionHandler.class);

    /**
     * Handles invalid GeoJSON polygons received through the REST API.
     */
    @ExceptionHandler(InvalidGeometryException.class)
    public ResponseEntity<ProblemDetail> handleInvalidGeometry(InvalidGeometryException ex) {
        log.debug("Invalid geometry received: {}", ex.getMessage());
        var reason = ex.getMessage() != null ? ex.getMessage() : "geometry is invalid";
        return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.invalidGeometry(reason));
    }
}
