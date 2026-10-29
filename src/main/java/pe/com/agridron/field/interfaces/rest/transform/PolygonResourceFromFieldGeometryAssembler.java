package pe.com.agridron.field.interfaces.rest.transform;

import org.locationtech.jts.geom.LineString;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.interfaces.rest.resources.PolygonResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Assembler that converts a domain {@link FieldGeometry} into its GeoJSON
 * {@link PolygonResource} representation.
 */
public final class PolygonResourceFromFieldGeometryAssembler {

    private static final String POLYGON_TYPE = "Polygon";

    private PolygonResourceFromFieldGeometryAssembler() {
    }

    /**
     * Converts the geometry into a GeoJSON polygon in WGS 84.
     *
     * @param geometry the domain geometry
     * @return the GeoJSON polygon, exterior ring first and holes afterwards
     */
    public static PolygonResource toResourceFromFieldGeometry(FieldGeometry geometry) {
        Objects.requireNonNull(geometry, "geometry is required");

        var polygon = geometry.polygon();
        var rings = new ArrayList<List<List<Double>>>(1 + polygon.getNumInteriorRing());
        rings.add(toPositions(polygon.getExteriorRing()));
        for (int index = 0; index < polygon.getNumInteriorRing(); index++) {
            rings.add(toPositions(polygon.getInteriorRingN(index)));
        }
        return new PolygonResource(POLYGON_TYPE, List.copyOf(rings));
    }

    private static List<List<Double>> toPositions(LineString ring) {
        return Arrays.stream(ring.getCoordinates())
                .map(coordinate -> List.of(coordinate.x, coordinate.y))
                .toList();
    }
}
