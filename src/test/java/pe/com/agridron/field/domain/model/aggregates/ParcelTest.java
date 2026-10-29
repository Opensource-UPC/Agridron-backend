package pe.com.agridron.field.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.events.ParcelCropInformationRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelGeometryUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelStatusChangedEvent;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParcelTest {

    @Test
    void createsAnActiveParcelAndDerivesItsArea() {
        var geometry = geometry(0.01);

        var parcel = Parcel.create(1L, "  Lote 1  ", geometry, null, null);

        assertThat(parcel.getId()).isNull();
        assertThat(parcel.getFarmId()).isEqualTo(1L);
        assertThat(parcel.getName()).isEqualTo("Lote 1");
        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.ACTIVE);
        assertThat(parcel.getGeometry().areaHectares()).isEqualTo(geometry.areaHectares());
        assertThat(parcel.getCropId()).isNull();
        assertThat(parcel.getPhenologicalStage()).isNull();
        assertThat(parcel.domainEvents()).isEmpty();
    }

    @Test
    void onCreatedRegistersParcelRegisteredEvent() {
        var parcel = Parcel.reconstitute(5L, 1L, null, "Lote 1", geometry(0.01), ParcelStatus.ACTIVE, null);

        parcel.onCreated();

        assertThat(parcel.domainEvents()).singleElement()
                .isInstanceOfSatisfying(ParcelRegisteredEvent.class, event -> {
                    assertThat(event.parcelId()).isEqualTo(5L);
                    assertThat(event.farmId()).isEqualTo(1L);
                    assertThat(event.areaHectares()).isPositive();
                });
    }

    @Test
    void onCreatedWithCropRegistersBothCreationEvents() {
        var parcel = Parcel.reconstitute(
                5L, 1L, 2L, "Lote 1", geometry(0.01), ParcelStatus.ACTIVE, PhenologicalStage.VEGETATIVE);

        parcel.onCreated();

        assertThat(parcel.domainEvents())
                .hasAtLeastOneElementOfType(ParcelRegisteredEvent.class)
                .hasAtLeastOneElementOfType(ParcelCropInformationRegisteredEvent.class);
    }

    @Test
    void rejectsCropWithoutPhenologicalStage() {
        assertThatThrownBy(() -> Parcel.create(1L, "Lote", geometry(0.01), 2L, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("phenologicalStage");
    }

    @Test
    void rejectsPhenologicalStageWithoutCrop() {
        assertThatThrownBy(() -> Parcel.create(1L, "Lote", geometry(0.01), null, PhenologicalStage.FLOWERING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("phenologicalStage");
    }

    @Test
    void rejectsNullGeometry() {
        assertThatThrownBy(() -> Parcel.create(1L, "Lote", null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("geometry");
    }

    @Test
    void rejectsInvalidFarmId() {
        assertThatThrownBy(() -> Parcel.create(0L, "Lote", geometry(0.01), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("farmId");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Parcel.create(1L, " ", geometry(0.01), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void reconstituteRejectsNullStatus() {
        assertThatThrownBy(() -> Parcel.reconstitute(
                5L, 1L, null, "Lote", geometry(0.01), null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("status");
    }

    @Test
    void redefiningGeometryRecalculatesAreaAndRegistersEvent() {
        var original = geometry(0.01);
        var parcel = Parcel.reconstitute(5L, 1L, null, "Lote", original, ParcelStatus.ACTIVE, null);

        parcel.redefineGeometry(geometry(0.02));

        assertThat(parcel.getGeometry().areaHectares()).isGreaterThan(original.areaHectares());
        assertThat(parcel.domainEvents()).singleElement()
                .isInstanceOfSatisfying(ParcelGeometryUpdatedEvent.class, event -> {
                    assertThat(event.parcelId()).isEqualTo(5L);
                    assertThat(event.areaHectares()).isEqualTo(parcel.getGeometry().areaHectares());
                });
    }

    @Test
    void updatesCropAndRegistersCropInformationEvent() {
        var parcel = Parcel.reconstitute(5L, 1L, null, "Lote", geometry(0.01), ParcelStatus.ACTIVE, null);

        parcel.updateDetails("Lote 1", 2L, PhenologicalStage.FLOWERING);

        assertThat(parcel.getName()).isEqualTo("Lote 1");
        assertThat(parcel.getCropId()).isEqualTo(2L);
        assertThat(parcel.getPhenologicalStage()).isEqualTo(PhenologicalStage.FLOWERING);
        assertThat(parcel.domainEvents()).singleElement()
                .isInstanceOfSatisfying(ParcelCropInformationRegisteredEvent.class, event -> {
                    assertThat(event.parcelId()).isEqualTo(5L);
                    assertThat(event.cropId()).isEqualTo(2L);
                    assertThat(event.phenologicalStage()).isEqualTo(PhenologicalStage.FLOWERING);
                });
    }

    @Test
    void updatingOnlyTheNameDoesNotRegisterCropInformationEvent() {
        var parcel = Parcel.reconstitute(
                5L, 1L, 2L, "Lote", geometry(0.01), ParcelStatus.ACTIVE, PhenologicalStage.FLOWERING);

        parcel.updateDetails("Lote 1", 2L, PhenologicalStage.FLOWERING);

        assertThat(parcel.getName()).isEqualTo("Lote 1");
        assertThat(parcel.domainEvents()).isEmpty();
    }

    @Test
    void removingCropInformationClearsTheStage() {
        var parcel = Parcel.reconstitute(
                5L, 1L, 2L, "Lote", geometry(0.01), ParcelStatus.ACTIVE, PhenologicalStage.FLOWERING);

        parcel.updateDetails("Lote", null, null);

        assertThat(parcel.getCropId()).isNull();
        assertThat(parcel.getPhenologicalStage()).isNull();
        assertThat(parcel.domainEvents()).isEmpty();
    }

    @Test
    void deactivatesParcelAndRegistersStatusEvent() {
        var parcel = Parcel.reconstitute(5L, 1L, null, "Lote", geometry(0.01), ParcelStatus.ACTIVE, null);

        parcel.deactivate();

        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.INACTIVE);
        assertThat(parcel.domainEvents()).singleElement()
                .isInstanceOfSatisfying(ParcelStatusChangedEvent.class, event -> {
                    assertThat(event.parcelId()).isEqualTo(5L);
                    assertThat(event.status()).isEqualTo(ParcelStatus.INACTIVE);
                });
    }

    @Test
    void deactivatingTwiceIsIdempotent() {
        var parcel = Parcel.reconstitute(5L, 1L, null, "Lote", geometry(0.01), ParcelStatus.INACTIVE, null);

        parcel.deactivate();

        assertThat(parcel.domainEvents()).isEmpty();
    }

    @Test
    void activatesAnInactiveParcel() {
        var parcel = Parcel.reconstitute(5L, 1L, null, "Lote", geometry(0.01), ParcelStatus.INACTIVE, null);

        parcel.activate();

        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.ACTIVE);
        assertThat(parcel.domainEvents()).singleElement()
                .isInstanceOfSatisfying(ParcelStatusChangedEvent.class, event ->
                        assertThat(event.status()).isEqualTo(ParcelStatus.ACTIVE));
    }

    @Test
    void activatingAnActiveParcelIsIdempotent() {
        var parcel = Parcel.reconstitute(5L, 1L, null, "Lote", geometry(0.01), ParcelStatus.ACTIVE, null);

        parcel.activate();

        assertThat(parcel.domainEvents()).isEmpty();
    }

    private static FieldGeometry geometry(double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(0, 0, size));
    }
}
