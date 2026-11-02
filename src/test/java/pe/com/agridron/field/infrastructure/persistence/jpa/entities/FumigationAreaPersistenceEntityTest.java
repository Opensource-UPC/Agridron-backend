package pe.com.agridron.field.infrastructure.persistence.jpa.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FumigationAreaPersistenceEntityTest extends AbstractFieldPersistenceTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsFumigationAreaWithGeometryAreaAndAuditingFields() {
        var parcelId = persistParcel();
        var geometry = geometry(0.001);
        var area = new FumigationAreaPersistenceEntity(
                parcelId, geometry.polygon(), geometry.areaHectares());

        var persisted = entityManager.persistFlushFind(area);

        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getParcelId()).isEqualTo(parcelId);
        assertThat(persisted.getAreaHectares()).isEqualTo(geometry.areaHectares());
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getUpdatedAt()).isNotNull();
        assertThat(persisted.getVersion()).isZero();
    }

    @Test
    void readsGeometryBackWithWgs84SridAndSameCoordinates() {
        var parcelId = persistParcel();
        var geometry = geometry(0.001);
        var persisted = entityManager.persistFlushFind(new FumigationAreaPersistenceEntity(
                parcelId, geometry.polygon(), geometry.areaHectares()));
        entityManager.clear();

        var reloaded = entityManager.find(FumigationAreaPersistenceEntity.class, persisted.getId());

        assertThat(reloaded.getGeometry().getSRID()).isEqualTo(4326);
        assertThat(reloaded.getGeometry().equalsExact(geometry.polygon())).isTrue();
        assertThat(reloaded.getAreaHectares()).isEqualTo(geometry.areaHectares());
    }

    @Test
    void removingParcelDeletesItsFumigationAreas() {
        var parcelId = persistParcel();
        var geometry = geometry(0.001);
        entityManager.persistFlushFind(new FumigationAreaPersistenceEntity(
                parcelId, geometry.polygon(), geometry.areaHectares()));

        entityManager.remove(entityManager.find(ParcelPersistenceEntity.class, parcelId));
        entityManager.flush();

        var remaining = ((Number) entityManager.getEntityManager()
                .createNativeQuery("select count(*) from fumigation_area where parcel_id = :parcelId")
                .setParameter("parcelId", parcelId)
                .getSingleResult()).longValue();
        assertThat(remaining).isZero();
    }

    @Test
    void rejectsNonPositiveArea() {
        var parcelId = persistParcel();
        var geometry = geometry(0.001);

        assertThatThrownBy(() -> {
            entityManager.persist(new FumigationAreaPersistenceEntity(
                    parcelId, geometry.polygon(), BigDecimal.ZERO));
            entityManager.flush();
        }).hasMessageContaining("ck_fumigation_area_positive");
    }

    private Long persistParcel() {
        var index = SEQUENCE.incrementAndGet();
        var farm = entityManager.persistFlushFind(new FarmPersistenceEntity(
                200L + index, "Finca " + index, "Lima", "Lima"));
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
