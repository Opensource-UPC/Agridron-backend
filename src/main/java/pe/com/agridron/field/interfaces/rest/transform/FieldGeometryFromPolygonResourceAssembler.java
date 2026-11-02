package pe.com.agridron.field.interfaces.rest.transform;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import pe.com.agridron.field.domain.exceptions.InvalidGeometryException;
import pe.com.agridron.field.domain.model.services.PolygonValidator;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.interfaces.rest.resources.PolygonResource;

import java.util.List;

/**
 * Assembler that converts a GeoJSON {@link PolygonResource} into the domain
 * {@link FieldGeometry} value object.
 *
 * <p>Structural problems (wrong type, malformed rings or positions) are reported as
 * {@link InvalidGeometryException}; the domain then validates topology, coordinate
 * range and area.</p>
 */
public final class FieldGeometryFromPolygonResourceAssembler {

    private static final String POLYGON_TYPE = "Polygon";
    private static final int MIN_RING_POSITIONS = 4;
    private static final int MIN_POSITION_DIMENSIONS = 2;
    private static final GeometryFactory WGS84_GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), PolygonValidator.WGS84_SRID);

    private FieldGeometryFromPolygonResourceAssembler() {
    }

    /**
     * Converts a GeoJSON polygon resource into a validated {@link FieldGeometry}.
     *
     * @param resource the GeoJSON polygon
     * @return the validated geometry
     * @throws InvalidGeometryException when the GeoJSON document or the polygon is invalid
     */
    public static FieldGeometry toFieldGeometryFromResource(PolygonResource resource) {
        if (resource == null) {
            throw new InvalidGeometryException("geometry is required");
        }
        if (!POLYGON_TYPE.equals(resource.type())) {
            throw new InvalidGeometryException(
                    "geometry type must be '%s'".formatted(POLYGON_TYPE));
        }

        var rings = resource.coordinates();
        if (rings == null || rings.isEmpty()) {
            throw new InvalidGeometryException("geometry must contain at least one ring");
        }

        var polygon = toPolygon(rings);
        return FieldGeometry.from(polygon);
    }

    private static Polygon toPolygon(List<List<List<Double>>> rings) {
        try {
            var shell = toRing(rings.getFirst(), "exterior ring");
            var holes = new LinearRing[rings.size() - 1];
            for (int index = 1; index < rings.size(); index++) {
                holes[index - 1] = toRing(rings.get(index), "interior ring " + (index - 1));
            }
            return WGS84_GEOMETRY_FACTORY.createPolygon(shell, holes);
        } catch (IllegalArgumentException ex) {
            var reason = ex.getMessage() != null ? ex.getMessage() : "geometry is invalid";
            throw new InvalidGeometryException(reason, ex);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidGeometryException("geometry has an incomplete ring", ex);
        }
    }

    private static LinearRing toRing(List<List<Double>> positions, String name) {
        if (positions == null || positions.size() < MIN_RING_POSITIONS) {
            throw new InvalidGeometryException(
                    "%s must have at least %d positions".formatted(name, MIN_RING_POSITIONS));
        }

        var coordinates = new Coordinate[positions.size()];
        for (int index = 0; index < positions.size(); index++) {
            coordinates[index] = toCoordinate(positions.get(index), name);
        }

        if (!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
            throw new InvalidGeometryException("%s must be closed".formatted(name));
        }
        return WGS84_GEOMETRY_FACTORY.createLinearRing(coordinates);
    }

    private static Coordinate toCoordinate(List<Double> position, String ringName) {
        if (position == null || position.size() < MIN_POSITION_DIMENSIONS) {
            throw new InvalidGeometryException(
                    "%s positions must contain longitude and latitude".formatted(ringName));
        }

        var longitude = position.get(0);
        var latitude = position.get(1);
        if (longitude == null || latitude == null
                || !Double.isFinite(longitude) || !Double.isFinite(latitude)) {
            throw new InvalidGeometryException(
                    "%s coordinates must be finite numbers".formatted(ringName));
        }
        return new Coordinate(longitude, latitude);
    }
}
