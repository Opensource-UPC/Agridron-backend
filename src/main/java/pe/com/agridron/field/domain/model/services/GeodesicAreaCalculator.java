package pe.com.agridron.field.domain.model.services;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes the geodesic area of a polygon on the WGS 84 ellipsoid.
 *
 * <p>Uses the spherical excess formula (Chamberlain and Duquette) with the mean Earth
 * radius. The result is accurate enough for agronomic parcels and is deterministic.
 * Polygons crossing the antimeridian are not supported.</p>
 */
public final class GeodesicAreaCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_008.8;
    private static final double SQUARE_METERS_PER_HECTARE = 10_000.0;
    private static final int HECTARE_SCALE = 4;

    private GeodesicAreaCalculator() {
    }

    /**
     * Calculates the area of the polygon in square meters.
     *
     * @param polygon the polygon to measure
     * @return the geodesic area in square meters
     */
    public static double areaInSquareMeters(Polygon polygon) {
        var exteriorArea = Math.abs(ringArea(polygon.getExteriorRing()));
        var holesArea = 0.0;
        for (int index = 0; index < polygon.getNumInteriorRing(); index++) {
            holesArea += Math.abs(ringArea(polygon.getInteriorRingN(index)));
        }
        return exteriorArea - holesArea;
    }

    /**
     * Calculates the area of the polygon in hectares, rounded to four decimals.
     *
     * @param polygon the polygon to measure
     * @return the geodesic area in hectares
     */
    public static BigDecimal areaInHectares(Polygon polygon) {
        var hectares = areaInSquareMeters(polygon) / SQUARE_METERS_PER_HECTARE;
        return BigDecimal.valueOf(hectares).setScale(HECTARE_SCALE, RoundingMode.HALF_UP);
    }

    private static double ringArea(LineString ring) {
        var coordinates = ring.getCoordinates();
        var sum = 0.0;
        for (int index = 0; index < coordinates.length - 1; index++) {
            var longitude1 = Math.toRadians(coordinates[index].x);
            var latitude1 = Math.toRadians(coordinates[index].y);
            var longitude2 = Math.toRadians(coordinates[index + 1].x);
            var latitude2 = Math.toRadians(coordinates[index + 1].y);
            sum += (longitude2 - longitude1) * (2 + Math.sin(latitude1) + Math.sin(latitude2));
        }
        return sum * EARTH_RADIUS_METERS * EARTH_RADIUS_METERS / 2.0;
    }
}
