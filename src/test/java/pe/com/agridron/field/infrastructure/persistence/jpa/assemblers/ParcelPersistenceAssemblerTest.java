package pe.com.agridron.field.infrastructure.persistence.jpa.assemblers;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.ParcelPersistenceEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ParcelPersistenceAssemblerTest {

    @Test
    void rebuildsDomainParcelFromEntity() {
        var geometry = geometry(0, 0, 0.01);
        var entity = new ParcelPersistenceEntity(
                1L, 2L, "Lote 1", geometry.polygon(), geometry.areaHectares(),
                ParcelStatus.ACTIVE, PhenologicalStage.FLOWERING);
        entity.setId(5L);

        var parcel = ParcelPersistenceAssembler.toDomainFromPersistence(entity);

        assertThat(parcel.getId()).isEqualTo(5L);
        assertThat(parcel.getFarmId()).isEqualTo(1L);
        assertThat(parcel.getCropId()).isEqualTo(2L);
        assertThat(parcel.getName()).isEqualTo("Lote 1");
        assertThat(parcel.getGeometry().areaHectares()).isEqualTo(geometry.areaHectares());
        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.ACTIVE);
        assertThat(parcel.getPhenologicalStage()).isEqualTo(PhenologicalStage.FLOWERING);
    }

    @Test
    void buildsEntityFromDomainParcel() {
        var geometry = geometry(0, 0, 0.01);
        var parcel = Parcel.reconstitute(
                5L, 1L, 2L, "Lote 1", geometry, ParcelStatus.INACTIVE, PhenologicalStage.HARVEST);

        var entity = ParcelPersistenceAssembler.toPersistenceFromDomain(parcel);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getFarmId()).isEqualTo(1L);
        assertThat(entity.getCropId()).isEqualTo(2L);
        assertThat(entity.getName()).isEqualTo("Lote 1");
        assertThat(entity.getGeometry().equalsExact(geometry.polygon())).isTrue();
        assertThat(entity.getAreaHectares()).isEqualTo(geometry.areaHectares());
        assertThat(entity.getStatus()).isEqualTo(ParcelStatus.INACTIVE);
        assertThat(entity.getPhenologicalStage()).isEqualTo(PhenologicalStage.HARVEST);
    }

    @Test
    void copiesMutableStateIntoAnExistingEntity() {
        var original = geometry(0, 0, 0.01);
        var updated = geometry(0.02, 0.02, 0.02);
        var parcel = Parcel.reconstitute(
                5L, 1L, 3L, "Lote 2", updated, ParcelStatus.INACTIVE, PhenologicalStage.MATURATION);
        var entity = new ParcelPersistenceEntity(
                1L, null, "Lote 1", original.polygon(), original.areaHectares(),
                ParcelStatus.ACTIVE, null);
        entity.setId(5L);

        ParcelPersistenceAssembler.copyToPersistence(parcel, entity);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getName()).isEqualTo("Lote 2");
        assertThat(entity.getCropId()).isEqualTo(3L);
        assertThat(entity.getGeometry().equalsExact(updated.polygon())).isTrue();
        assertThat(entity.getAreaHectares()).isEqualTo(updated.areaHectares());
        assertThat(entity.getStatus()).isEqualTo(ParcelStatus.INACTIVE);
        assertThat(entity.getPhenologicalStage()).isEqualTo(PhenologicalStage.MATURATION);
    }

    @Test
    void keepsNullCropInformation() {
        var geometry = geometry(0, 0, 0.01);
        var parcel = Parcel.reconstitute(5L, 1L, null, "Lote 1", geometry, ParcelStatus.ACTIVE, null);

        var entity = ParcelPersistenceAssembler.toPersistenceFromDomain(parcel);

        assertThat(entity.getCropId()).isNull();
        assertThat(entity.getPhenologicalStage()).isNull();
    }

    @Test
    void returnsNullWhenSourceIsNull() {
        assertThat(ParcelPersistenceAssembler.toDomainFromPersistence(null)).isNull();
        assertThat(ParcelPersistenceAssembler.toPersistenceFromDomain(null)).isNull();
    }

    private static FieldGeometry geometry(double minLongitude, double minLatitude, double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(minLongitude, minLatitude, size));
    }
}
