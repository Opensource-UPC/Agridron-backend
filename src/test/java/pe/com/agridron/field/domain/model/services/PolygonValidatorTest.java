package pe.com.agridron.field.domain.model.services;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.exceptions.InvalidGeometryException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolygonValidatorTest {

    @Test
    void acceptsAValidPolygon() {
        assertThatCode(() -> PolygonValidator.validate(TestGeometries.wgs84Square(0, 0, 0.01)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsNullPolygon() {
        assertThatThrownBy(() -> PolygonValidator.validate(null))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("required");
    }

    @Test
    void rejectsEmptyPolygon() {
        assertThatThrownBy(() -> PolygonValidator.validate(TestGeometries.emptyPolygon()))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void rejectsPolygonWithoutWgs84Srid() {
        assertThatThrownBy(() -> PolygonValidator.validate(TestGeometries.squareWithSrid(0, 0, 0, 0.01)))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("SRID 4326");
    }

    @Test
    void rejectsSelfIntersectingPolygon() {
        assertThatThrownBy(() -> PolygonValidator.validate(TestGeometries.selfIntersectingPolygon()))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("topologically valid");
    }

    @Test
    void rejectsHoleOutsideTheExteriorRing() {
        assertThatThrownBy(() -> PolygonValidator.validate(TestGeometries.holeOutsideShell()))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("topologically valid");
    }

    @Test
    void rejectsCoordinatesOutsideTheWgs84Range() {
        assertThatThrownBy(() -> PolygonValidator.validate(TestGeometries.outOfRangePolygon()))
                .isInstanceOf(InvalidGeometryException.class)
                .hasMessageContaining("longitude");
    }

    @Test
    void rejectsDegeneratePolygon() {
        assertThatThrownBy(() -> PolygonValidator.validate(TestGeometries.degeneratePolygon()))
                .isInstanceOf(InvalidGeometryException.class);
    }
}
