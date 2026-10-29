package pe.com.agridron.field.interfaces.rest.transform;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.exceptions.InvalidGeometryException;
import pe.com.agridron.field.interfaces.rest.resources.PolygonResource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldGeometryFromPolygonResourceAssemblerTest {

    @Test
    void convertsValidPolygon() {
        var geometry = FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(square(-77.042, -12.046, 0.001));

        assertThat(geometry.polygon().getSRID()).isEqualTo(4326);
        assertThat(geometry.polygon().getNumPoints()).isEqualTo(5);
        assertThat(geometry.polygon().getNumInteriorRing()).isZero();
        assertThat(geometry.areaHectares()).isPositive();
    }

    @Test
    void convertsPolygonWithHole() {
        var resource = new PolygonResource("Polygon", List.of(
                ring(-77.042, -12.046, 0.01),
                ring(-77.040, -12.044, 0.002)));

        var geometry = FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource);

        assertThat(geometry.polygon().getNumInteriorRing()).isEqualTo(1);
    }

    @Test
    void acceptsPositionsWithAltitude() {
        var exterior = List.of(
                List.of(-77.042, -12.046, 120.0),
                List.of(-77.041, -12.046, 120.0),
                List.of(-77.041, -12.045, 120.0),
                List.of(-77.042, -12.045, 120.0),
                List.of(-77.042, -12.046, 120.0));
        var resource = new PolygonResource("Polygon", List.of(exterior));

        var geometry = FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource);

        assertThat(geometry.polygon().getCoordinates())
                .extracting(coordinate -> coordinate.y)
                .doesNotContain(120.0);
    }

    @Test
    void rejectsNullResource() {
        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(null))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("geometry is required");
    }

    @Test
    void rejectsNonPolygonType() {
        var resource = new PolygonResource("Point", List.of(ring(-77.042, -12.046, 0.001)));

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("geometry type must be 'Polygon'");
    }

    @Test
    void rejectsNullType() {
        var resource = new PolygonResource(null, List.of(ring(-77.042, -12.046, 0.001)));

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("geometry type must be 'Polygon'");
    }

    @Test
    void rejectsNullCoordinates() {
        var resource = new PolygonResource("Polygon", null);

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("geometry must contain at least one ring");
    }

    @Test
    void rejectsEmptyCoordinates() {
        var resource = new PolygonResource("Polygon", List.of());

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("geometry must contain at least one ring");
    }

    @Test
    void rejectsRingWithTooFewPositions() {
        var resource = new PolygonResource("Polygon", List.of(List.of(
                List.of(-77.042, -12.046),
                List.of(-77.041, -12.046),
                List.of(-77.042, -12.046))));

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("exterior ring must have at least 4 positions");
    }

    @Test
    void rejectsOpenRing() {
        var resource = new PolygonResource("Polygon", List.of(List.of(
                List.of(-77.042, -12.046),
                List.of(-77.041, -12.046),
                List.of(-77.041, -12.045),
                List.of(-77.042, -12.045),
                List.of(-77.042, -12.0465))));

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("exterior ring must be closed");
    }

    @Test
    void rejectsIncompletePosition() {
        var resource = new PolygonResource("Polygon", List.of(List.of(
                List.of(-77.042),
                List.of(-77.041, -12.046),
                List.of(-77.041, -12.045),
                List.of(-77.042, -12.045),
                List.of(-77.042))));

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("exterior ring positions must contain longitude and latitude");
    }

    @Test
    void rejectsNonFiniteCoordinate() {
        var exterior = List.of(
                Arrays.asList(-77.042, -12.046),
                Arrays.asList(-77.041, Double.NaN),
                Arrays.asList(-77.041, -12.045),
                Arrays.asList(-77.042, -12.045),
                Arrays.asList(-77.042, -12.046));
        var resource = new PolygonResource("Polygon", List.of(exterior));

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("exterior ring coordinates must be finite numbers");
    }

    @Test
    void rejectsLongitudeOutOfRange() {
        var resource = square(181, 0, 0.01);

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("longitude must be between -180 and 180");
    }

    @Test
    void rejectsLatitudeOutOfRange() {
        var resource = square(0, 91, 0.01);

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("latitude must be between -90 and 90");
    }

    @Test
    void rejectsSelfIntersectingPolygon() {
        var exterior = List.of(
                List.of(0.0, 0.0),
                List.of(0.01, 0.01),
                List.of(0.01, 0.0),
                List.of(0.0, 0.01),
                List.of(0.0, 0.0));
        var resource = new PolygonResource("Polygon", List.of(exterior));

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessage("geometry is not topologically valid");
    }

    @Test
    void rejectsGeometryWithoutArea() {
        var exterior = List.of(
                List.of(0.0, 0.0),
                List.of(0.001, 0.0),
                List.of(0.002, 0.0),
                List.of(0.0, 0.0));
        var resource = new PolygonResource("Polygon", List.of(exterior));

        assertThatThrownBy(() -> FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource))
                .isInstanceOf(InvalidGeometryException.class);
    }

    private static PolygonResource square(double minLongitude, double minLatitude, double size) {
        return new PolygonResource("Polygon", List.of(ring(minLongitude, minLatitude, size)));
    }

    private static List<List<Double>> ring(double minLongitude, double minLatitude, double size) {
        return List.of(
                List.of(minLongitude, minLatitude),
                List.of(minLongitude + size, minLatitude),
                List.of(minLongitude + size, minLatitude + size),
                List.of(minLongitude, minLatitude + size),
                List.of(minLongitude, minLatitude));
    }
}
