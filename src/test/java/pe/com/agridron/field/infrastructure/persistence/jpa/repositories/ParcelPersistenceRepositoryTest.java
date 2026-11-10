package pe.com.agridron.field.infrastructure.persistence.jpa.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.CropPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.ParcelPersistenceEntity;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParcelPersistenceRepositoryTest extends AbstractFieldPersistenceTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @Autowired
    private ParcelPersistenceRepository parcelPersistenceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findsParcelsOfFarmOrderedByName() {
        var farmId = persistFarm();
        persistParcel(farmId, "Zeta", null);
        persistParcel(farmId, "Alfa", null);
        var otherFarmId = persistFarm();
        persistParcel(otherFarmId, "Otra", null);

        var parcels = parcelPersistenceRepository.findAllByFarmIdOrderByNameAsc(farmId);

        assertThat(parcels).extracting(ParcelPersistenceEntity::getName)
                .containsExactly("Alfa", "Zeta");
    }

    @Test
    void detectsDuplicatedNameForFarm() {
        var farmId = persistFarm();
        var parcel = persistParcel(farmId, "Lote 1", null);

        assertThat(parcelPersistenceRepository.existsByFarmIdAndName(farmId, "Lote 1")).isTrue();
        assertThat(parcelPersistenceRepository.existsByFarmIdAndName(farmId, "Lote 2")).isFalse();
        assertThat(parcelPersistenceRepository.existsByFarmIdAndNameAndIdNot(farmId, "Lote 1", parcel.getId()))
                .isFalse();
        assertThat(parcelPersistenceRepository.existsByFarmIdAndNameAndIdNot(farmId, "Lote 1", 999_999L))
                .isTrue();
    }

    @Test
    void rejectsDuplicatedNameForSameFarm() {
        var farmId = persistFarm();
        persistParcel(farmId, "Lote 1", null);

        assertThatThrownBy(() -> parcelPersistenceRepository.saveAndFlush(newParcel(farmId, "Lote 1", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsSameNameForDifferentFarms() {
        var firstFarmId = persistFarm();
        var secondFarmId = persistFarm();
        persistParcel(firstFarmId, "Lote 1", null);

        var other = parcelPersistenceRepository.saveAndFlush(newParcel(secondFarmId, "Lote 1", null));

        assertThat(other.getId()).isNotNull();
    }

    @Test
    void linksParcelToCrop() {
        var farmId = persistFarm();
        var cropId = persistCrop();
        var parcel = persistParcel(farmId, "Lote 1", cropId);

        assertThat(parcel.getCropId()).isEqualTo(cropId);
    }

    private ParcelPersistenceEntity persistParcel(Long farmId, String name, Long cropId) {
        return entityManager.persistFlushFind(newParcel(farmId, name, cropId));
    }

    private ParcelPersistenceEntity newParcel(Long farmId, String name, Long cropId) {
        var geometry = FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, 0.01));
        var stage = cropId == null ? null : pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage.VEGETATIVE;
        return new ParcelPersistenceEntity(
                farmId, cropId, name, geometry.polygon(), geometry.areaHectares(),
                ParcelStatus.ACTIVE, stage);
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
}
