package pe.com.agridron.field.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.events.FumigationAreaDelimitedEvent;
import pe.com.agridron.field.domain.exceptions.FumigationAreaOutsideParcelException;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FumigationAreaTest {

    private final FieldGeometry parcelGeometry = geometry(0, 0, 0.01);

    @Test
    void delimitsAnAreaContainedInTheParcel() {
        var area = FumigationArea.delimit(1L, parcelGeometry, geometry(0.002, 0.002, 0.002));

        assertThat(area.getId()).isNull();
        assertThat(area.getParcelId()).isEqualTo(1L);
        assertThat(area.getGeometry().areaHectares()).isPositive();
        assertThat(area.domainEvents()).isEmpty();
    }

    @Test
    void onCreatedRegistersDelimitedEventWithIdentity() {
        var area = FumigationArea.reconstitute(7L, 1L, geometry(0.002, 0.002, 0.002));

        area.onCreated();

        assertThat(area.domainEvents()).singleElement()
                .isInstanceOfSatisfying(FumigationAreaDelimitedEvent.class, event -> {
                    assertThat(event.fumigationAreaId()).isEqualTo(7L);
                    assertThat(event.parcelId()).isEqualTo(1L);
                    assertThat(event.areaHectares()).isPositive();
                });
    }

    @Test
    void allowsAnAreaEqualToTheParcel() {
        assertThatCode(() -> FumigationArea.delimit(1L, parcelGeometry, parcelGeometry))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAnAreaOutsideTheParcel() {
        assertThatThrownBy(() -> FumigationArea.delimit(
                1L, parcelGeometry, geometry(0.02, 0.02, 0.002)))
                .isInstanceOf(FumigationAreaOutsideParcelException.class)
                .hasMessageContaining("contained");
    }

    @Test
    void rejectsAnAreaPartiallyOutsideTheParcel() {
        assertThatThrownBy(() -> FumigationArea.delimit(
                1L, parcelGeometry, geometry(0.009, 0.009, 0.002)))
                .isInstanceOf(FumigationAreaOutsideParcelException.class)
                .hasMessageContaining("contained");
    }

    @Test
    void rejectsAnAreaLargerThanTheParcel() {
        assertThatThrownBy(() -> FumigationArea.delimit(
                1L, parcelGeometry, geometry(0, 0, 0.02)))
                .isInstanceOf(FumigationAreaOutsideParcelException.class);
    }

    @Test
    void redefinesTheAreaAndRegistersEvent() {
        var area = FumigationArea.reconstitute(7L, 1L, geometry(0.002, 0.002, 0.002));

        area.redefine(parcelGeometry, geometry(0.003, 0.003, 0.003));

        assertThat(area.getGeometry().areaHectares()).isPositive();
        assertThat(area.domainEvents()).singleElement()
                .isInstanceOfSatisfying(FumigationAreaDelimitedEvent.class, event ->
                        assertThat(event.fumigationAreaId()).isEqualTo(7L));
    }

    @Test
    void redefiningOutsideTheParcelThrows() {
        var area = FumigationArea.reconstitute(7L, 1L, geometry(0.002, 0.002, 0.002));

        assertThatThrownBy(() -> area.redefine(parcelGeometry, geometry(0.02, 0.02, 0.002)))
                .isInstanceOf(FumigationAreaOutsideParcelException.class);
    }

    @Test
    void rejectsInvalidParcelId() {
        assertThatThrownBy(() -> FumigationArea.delimit(
                0L, parcelGeometry, geometry(0.002, 0.002, 0.002)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("parcelId");
    }

    @Test
    void rejectsNullParcelGeometry() {
        assertThatThrownBy(() -> FumigationArea.delimit(
                1L, null, geometry(0.002, 0.002, 0.002)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("parcelGeometry");
    }

    private static FieldGeometry geometry(double minLongitude, double minLatitude, double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(minLongitude, minLatitude, size));
    }
}
