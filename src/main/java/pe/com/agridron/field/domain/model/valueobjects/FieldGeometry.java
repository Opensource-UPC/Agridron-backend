package pe.com.agridron.field.domain.model.valueobjects;

import org.locationtech.jts.geom.Polygon;
import pe.com.agridron.field.domain.exceptions.InvalidGeometryException;
import pe.com.agridron.field.domain.model.services.GeodesicAreaCalculator;
import pe.com.agridron.field.domain.model.services.PolygonValidator;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A validated WGS 84 polygon together with its geodesic area.
 *
 * <p>The area is always derived from the geometry, so callers cannot provide an
 * inconsistent value. Instances are immutable by convention; the polygon must not be
 * modified after construction.</p>
 */
public final class FieldGeometry {

    private final Polygon polygon;
    private final BigDecimal areaHectares;

    private FieldGeometry(Polygon polygon, BigDecimal areaHectares) {
        this.polygon = polygon;
        this.areaHectares = areaHectares;
    }

    /**
     * Validates the polygon and calculates its area.
     *
     * @param polygon a WGS 84 polygon
     * @return the validated geometry
     * @throws InvalidGeometryException when the polygon is invalid or has no area
     */
    public static FieldGeometry from(Polygon polygon) {
        PolygonValidator.validate(polygon);
        var areaHectares = GeodesicAreaCalculator.areaInHectares(polygon);
        if (areaHectares.signum() <= 0) {
            throw new InvalidGeometryException("geometry must enclose an area greater than zero");
        }
        return new FieldGeometry(polygon, areaHectares);
    }

    /**
     * Returns the underlying polygon. Callers must treat it as read-only.
     */
    public Polygon polygon() {
        return polygon;
    }

    /**
     * Returns the geodesic area in hectares, rounded to four decimals.
     */
    public BigDecimal areaHectares() {
        return areaHectares;
    }

    /**
     * Checks whether this geometry covers the other one.
     *
     * @param other the geometry to check
     * @return {@code true} when every point of {@code other} is inside or on the boundary
     */
    public boolean covers(FieldGeometry other) {
        Objects.requireNonNull(other, "other geometry is required");
        return polygon.covers(other.polygon);
    }

    /**
     * Checks whether this geometry is covered by the container geometry.
     *
     * @param container the candidate container
     * @return {@code true} when this geometry is inside or on the boundary of {@code container}
     */
    public boolean isCoveredBy(FieldGeometry container) {
        Objects.requireNonNull(container, "container geometry is required");
        return container.covers(this);
    }

    @Override
    public String toString() {
        return "FieldGeometry[areaHectares=%s]".formatted(areaHectares);
    }
}
