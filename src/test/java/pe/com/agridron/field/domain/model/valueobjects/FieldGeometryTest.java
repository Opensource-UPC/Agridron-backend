package pe.com.agridron.field.domain.model.valueobjects;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.exceptions.InvalidGeometryException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldGeometryTest {

    @Test
    void createsValidGeometryAndComputesAreaInHectares() {
        var geometry = FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, 0.001));

        assertThat(geometry.polygon().getSRID()).isEqualTo(4326);
        assertThat(geometry.areaHectares())
                .isGreaterThan(new BigDecimal("1.0"))
                .isLessThan(new BigDecimal("1.4"));
    }

    @Test
    void rejectsNullPolygon() {
        assertThatThrownBy(() -> FieldGeometry.from(null))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("required");
    }

    @Test
    void rejectsNonWgs84Srid() {
        var polygon = TestGeometries.squareWithSrid(0, 0, 0, 0.01);

        assertThatThrownBy(() -> FieldGeometry.from(polygon))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("SRID 4326");
    }

    @Test
    void rejectsSelfIntersectingPolygon() {
        assertThatThrownBy(() -> FieldGeometry.from(TestGeometries.selfIntersectingPolygon()))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("topologically valid");
    }

    @Test
    void rejectsCoordinatesOutsideWgs84Range() {
        assertThatThrownBy(() -> FieldGeometry.from(TestGeometries.outOfRangePolygon()))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("longitude");
    }

    @Test
    void rejectsEmptyPolygon() {
        assertThatThrownBy(() -> FieldGeometry.from(TestGeometries.emptyPolygon()))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void rejectsDegeneratePolygon() {
        assertThatThrownBy(() -> FieldGeometry.from(TestGeometries.degeneratePolygon()))
                .isInstanceOf(InvalidGeometryException.class);
    }

    @Test
    void subtractsHolesFromArea() {
        var withHole = FieldGeometry.from(TestGeometries.wgs84PolygonWithHole());
        var withoutHole = FieldGeometry.from(TestGeometries.wgs84Square(0, 0, 0.01));

        assertThat(withHole.areaHectares()).isLessThan(withoutHole.areaHectares());
    }

    @Test
    void coversGeometryInsideItsBoundary() {
        var outer = FieldGeometry.from(TestGeometries.wgs84Square(0, 0, 0.01));
        var inner = FieldGeometry.from(TestGeometries.wgs84Square(0.002, 0.002, 0.002));

        assertThat(outer.covers(inner)).isTrue();
        assertThat(inner.isCoveredBy(outer)).isTrue();
    }

    @Test
    void doesNotCoverGeometryOutsideItsBoundary() {
        var outer = FieldGeometry.from(TestGeometries.wgs84Square(0, 0, 0.01));
        var outside = FieldGeometry.from(TestGeometries.wgs84Square(0.02, 0.02, 0.002));

        assertThat(outer.covers(outside)).isFalse();
        assertThat(outside.isCoveredBy(outer)).isFalse();
    }
}
