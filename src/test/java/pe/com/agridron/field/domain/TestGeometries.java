package pe.com.agridron.field.domain;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Reusable polygon fixtures for Field Management domain tests.
 */
public final class TestGeometries {

    private static final GeometryFactory WGS84_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private TestGeometries() {
    }

    public static Polygon wgs84Square(double minLongitude, double minLatitude, double size) {
        return square(WGS84_FACTORY, minLongitude, minLatitude, size);
    }

    public static Polygon squareWithSrid(int srid, double minLongitude, double minLatitude, double size) {
        return square(new GeometryFactory(new PrecisionModel(), srid), minLongitude, minLatitude, size);
    }

    public static Polygon wgs84PolygonWithHole() {
        var shell = WGS84_FACTORY.createLinearRing(closedRing(0, 0, 0.01));
        var hole = WGS84_FACTORY.createLinearRing(closedRing(0.004, 0.004, 0.002));
        return WGS84_FACTORY.createPolygon(shell, new LinearRing[]{hole});
    }

    public static Polygon holeOutsideShell() {
        var shell = WGS84_FACTORY.createLinearRing(closedRing(0, 0, 0.01));
        var hole = WGS84_FACTORY.createLinearRing(closedRing(1, 1, 0.002));
        return WGS84_FACTORY.createPolygon(shell, new LinearRing[]{hole});
    }

    public static Polygon selfIntersectingPolygon() {
        return WGS84_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0.01, 0.01),
                new Coordinate(0.01, 0),
                new Coordinate(0, 0.01),
                new Coordinate(0, 0)
        });
    }

    public static Polygon degeneratePolygon() {
        return WGS84_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0.001, 0),
                new Coordinate(0.002, 0),
                new Coordinate(0, 0)
        });
    }

    public static Polygon emptyPolygon() {
        return WGS84_FACTORY.createPolygon();
    }

    public static Polygon outOfRangePolygon() {
        return wgs84Square(181, 0, 0.01);
    }

    private static Polygon square(GeometryFactory factory, double minLongitude, double minLatitude, double size) {
        return factory.createPolygon(closedRing(minLongitude, minLatitude, size));
    }

    private static Coordinate[] closedRing(double minLongitude, double minLatitude, double size) {
        return new Coordinate[]{
                new Coordinate(minLongitude, minLatitude),
                new Coordinate(minLongitude + size, minLatitude),
                new Coordinate(minLongitude + size, minLatitude + size),
                new Coordinate(minLongitude, minLatitude + size),
                new Coordinate(minLongitude, minLatitude)
        };
    }
}
