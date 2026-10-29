package pe.com.agridron.field.infrastructure.persistence.jpa.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParcelPersistenceEntityTest extends AbstractFieldPersistenceTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsParcelWithGeometryAreaAndAuditingFields() {
        var farmId = persistFarm();
        var geometry = geometry(-77.042, -12.046, 0.01);
        var parcel = new ParcelPersistenceEntity(
                farmId, null, "Lote 1", geometry.polygon(), geometry.areaHectares(),
                ParcelStatus.ACTIVE, null);

        var persisted = entityManager.persistFlushFind(parcel);

        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getFarmId()).isEqualTo(farmId);
        assertThat(persisted.getCropId()).isNull();
        assertThat(persisted.getName()).isEqualTo("Lote 1");
        assertThat(persisted.getAreaHectares()).isEqualTo(geometry.areaHectares());
        assertThat(persisted.getStatus()).isEqualTo(ParcelStatus.ACTIVE);
        assertThat(persisted.getPhenologicalStage()).isNull();
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getUpdatedAt()).isNotNull();
        assertThat(persisted.getVersion()).isZero();
    }

    @Test
    void readsGeometryBackWithWgs84SridAndSameCoordinates() {
        var farmId = persistFarm();
        var geometry = geometry(-77.042, -12.046, 0.001);
        var persisted = entityManager.persistFlushFind(new ParcelPersistenceEntity(
                farmId, null, "Lote 1", geometry.polygon(), geometry.areaHectares(),
                ParcelStatus.ACTIVE, null));
        entityManager.clear();

        var reloaded = entityManager.find(ParcelPersistenceEntity.class, persisted.getId());

        assertThat(reloaded.getGeometry().getSRID()).isEqualTo(4326);
        assertThat(reloaded.getGeometry().equalsExact(geometry.polygon())).isTrue();
        assertThat(reloaded.getAreaHectares()).isEqualTo(geometry.areaHectares());
    }

    @Test
    void persistsCropInformation() {
        var farmId = persistFarm();
        var cropId = persistCrop();
        var geometry = geometry(-77.042, -12.046, 0.01);
        var persisted = entityManager.persistFlushFind(new ParcelPersistenceEntity(
                farmId, cropId, "Lote 1", geometry.polygon(), geometry.areaHectares(),
                ParcelStatus.ACTIVE, PhenologicalStage.VEGETATIVE));
        entityManager.clear();

        var reloaded = entityManager.find(ParcelPersistenceEntity.class, persisted.getId());

        assertThat(reloaded.getCropId()).isEqualTo(cropId);
        assertThat(reloaded.getPhenologicalStage()).isEqualTo(PhenologicalStage.VEGETATIVE);
    }

    @Test
    void rejectsCropWithoutPhenologicalStage() {
        var farmId = persistFarm();
        var cropId = persistCrop();
        var geometry = geometry(-77.042, -12.046, 0.01);

        assertThatThrownBy(() -> {
            entityManager.persist(new ParcelPersistenceEntity(
                    farmId, cropId, "Lote 1", geometry.polygon(), geometry.areaHectares(),
                    ParcelStatus.ACTIVE, null));
            entityManager.flush();
        }).hasMessageContaining("ck_parcel_crop_stage");
    }

    @Test
    void rejectsPhenologicalStageWithoutCrop() {
        var farmId = persistFarm();
        var geometry = geometry(-77.042, -12.046, 0.01);

        assertThatThrownBy(() -> {
            entityManager.persist(new ParcelPersistenceEntity(
                    farmId, null, "Lote 1", geometry.polygon(), geometry.areaHectares(),
                    ParcelStatus.ACTIVE, PhenologicalStage.FLOWERING));
            entityManager.flush();
        }).hasMessageContaining("ck_parcel_crop_stage");
    }

    @Test
    void rejectsNonPositiveArea() {
        var farmId = persistFarm();
        var geometry = geometry(-77.042, -12.046, 0.01);

        assertThatThrownBy(() -> {
            entityManager.persist(new ParcelPersistenceEntity(
                    farmId, null, "Lote 1", geometry.polygon(), java.math.BigDecimal.ZERO,
                    ParcelStatus.ACTIVE, null));
            entityManager.flush();
        }).hasMessageContaining("ck_parcel_area_positive");
    }

    private Long persistFarm() {
        var index = SEQUENCE.incrementAndGet();
        var farm = entityManager.persistFlushFind(new FarmPersistenceEntity(
                100L + index, "Finca " + index, "Lima", "Lima"));
        return farm.getId();
    }

    private Long persistCrop() {
        var index = SEQUENCE.incrementAndGet();
        var crop = entityManager.persistFlushFind(new CropPersistenceEntity(
                "Cultivo " + index, "Variedad " + index));
        return crop.getId();
    }

    private static FieldGeometry geometry(double minLongitude, double minLatitude, double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(minLongitude, minLatitude, size));
    }
}
