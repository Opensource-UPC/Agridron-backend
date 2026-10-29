package pe.com.agridron.field.infrastructure.persistence.jpa.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FumigationAreaPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.ParcelPersistenceEntity;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class FumigationAreaPersistenceRepositoryTest extends AbstractFieldPersistenceTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @Autowired
    private FumigationAreaPersistenceRepository fumigationAreaPersistenceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findsAreasOfParcelOrderedById() {
        var firstParcelId = persistParcel();
        var secondParcelId = persistParcel();
        var first = persistArea(firstParcelId);
        var second = persistArea(firstParcelId);
        persistArea(secondParcelId);

        var areas = fumigationAreaPersistenceRepository.findAllByParcelIdOrderByIdAsc(firstParcelId);

        assertThat(areas).extracting(FumigationAreaPersistenceEntity::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void linksAreaToItsParcel() {
        var parcelId = persistParcel();
        var area = persistArea(parcelId);

        assertThat(area.getParcelId()).isEqualTo(parcelId);
    }

    private FumigationAreaPersistenceEntity persistArea(Long parcelId) {
        var geometry = geometry(0.001);
        return entityManager.persistFlushFind(new FumigationAreaPersistenceEntity(
                parcelId, geometry.polygon(), geometry.areaHectares()));
    }

    private Long persistParcel() {
        var index = SEQUENCE.incrementAndGet();
        var farm = entityManager.persistFlushFind(new FarmPersistenceEntity(
                300L + index, "Finca " + index, "Lima", "Lima"));
        var geometry = geometry(0.01);
        var parcel = entityManager.persistFlushFind(new ParcelPersistenceEntity(
                farm.getId(), null, "Lote " + index, geometry.polygon(), geometry.areaHectares(),
                ParcelStatus.ACTIVE, null));
        return parcel.getId();
    }

    private static FieldGeometry geometry(double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, size));
    }
}
