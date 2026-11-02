package pe.com.agridron.field.interfaces.rest.transform;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolygonResourceFromFieldGeometryAssemblerTest {

    @Test
    void convertsExteriorRingIntoClosedGeoJsonRing() {
        var geometry = FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, 0.001));

        var resource = PolygonResourceFromFieldGeometryAssembler
                .toResourceFromFieldGeometry(geometry);

        assertThat(resource.type()).isEqualTo("Polygon");
        assertThat(resource.coordinates()).hasSize(1);
        var exterior = resource.coordinates().getFirst();
        assertThat(exterior).hasSize(5);
        assertThat(exterior.getFirst()).containsExactly(-77.042, -12.046);
        assertThat(exterior.getLast()).isEqualTo(exterior.getFirst());
    }

    @Test
    void convertsHolesAfterTheExteriorRing() {
        var geometry = FieldGeometry.from(TestGeometries.wgs84PolygonWithHole());

        var resource = PolygonResourceFromFieldGeometryAssembler
                .toResourceFromFieldGeometry(geometry);

        assertThat(resource.coordinates()).hasSize(2);
        assertThat(resource.coordinates().get(1)).hasSize(5);
    }

    @Test
    void roundTripsGeometryThroughGeoJson() {
        var original = FieldGeometry.from(TestGeometries.wgs84PolygonWithHole());

        var resource = PolygonResourceFromFieldGeometryAssembler
                .toResourceFromFieldGeometry(original);
        var restored = FieldGeometryFromPolygonResourceAssembler
                .toFieldGeometryFromResource(resource);

        assertThat(restored.areaHectares()).isEqualByComparingTo(original.areaHectares());
        assertThat(restored.polygon().equalsExact(original.polygon())).isTrue();
    }

    @Test
    void rejectsNullGeometry() {
        assertThatThrownBy(() -> PolygonResourceFromFieldGeometryAssembler
                .toResourceFromFieldGeometry(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("geometry is required");
    }
}
