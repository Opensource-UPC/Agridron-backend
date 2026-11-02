package pe.com.agridron.field.domain.model.services;

import pe.com.agridron.field.domain.exceptions.InvalidGeometryException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * Validates the polygons accepted by Field Management.
 *
 * <p>A valid field polygon is a non-empty JTS {@link Polygon} in WGS 84 (SRID 4326),
 * topologically valid, with a closed exterior ring, at least four positions and every
 * coordinate inside the longitude/latitude ranges. Holes are allowed.</p>
 */
public final class PolygonValidator {

    public static final int WGS84_SRID = 4326;

    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final int MIN_RING_POSITIONS = 4;

    private PolygonValidator() {
    }

    /**
     * Validates a polygon or throws {@link InvalidGeometryException} describing the first violation.
     *
     * @param polygon the polygon to validate
     */
    public static void validate(Polygon polygon) {
        if (polygon == null) {
            throw new InvalidGeometryException("geometry is required");
        }
        if (polygon.isEmpty()) {
            throw new InvalidGeometryException("geometry must not be empty");
        }
        if (polygon.getSRID() != WGS84_SRID) {
            throw new InvalidGeometryException("geometry must use SRID %d".formatted(WGS84_SRID));
        }
        if (!polygon.isValid()) {
            throw new InvalidGeometryException("geometry is not topologically valid");
        }
        if (polygon.getArea() <= 0) {
            throw new InvalidGeometryException("geometry must enclose an area greater than zero");
        }

        validateRing(polygon.getExteriorRing(), "exterior ring");
        for (int index = 0; index < polygon.getNumInteriorRing(); index++) {
            validateRing(polygon.getInteriorRingN(index), "interior ring " + index);
        }
        validateCoordinates(polygon);
    }

    private static void validateRing(LinearRing ring, String name) {
        if (ring.getNumPoints() < MIN_RING_POSITIONS) {
            throw new InvalidGeometryException("%s must have at least %d positions".formatted(name, MIN_RING_POSITIONS));
        }
        if (!ring.isClosed()) {
            throw new InvalidGeometryException("%s must be closed".formatted(name));
        }
    }

    private static void validateCoordinates(Polygon polygon) {
        for (Coordinate coordinate : polygon.getCoordinates()) {
            if (!Double.isFinite(coordinate.x) || !Double.isFinite(coordinate.y)) {
                throw new InvalidGeometryException("geometry coordinates must be finite");
            }
            if (coordinate.x < MIN_LONGITUDE || coordinate.x > MAX_LONGITUDE) {
                throw new InvalidGeometryException("longitude must be between -180 and 180");
            }
            if (coordinate.y < MIN_LATITUDE || coordinate.y > MAX_LATITUDE) {
                throw new InvalidGeometryException("latitude must be between -90 and 90");
            }
        }
    }
}
