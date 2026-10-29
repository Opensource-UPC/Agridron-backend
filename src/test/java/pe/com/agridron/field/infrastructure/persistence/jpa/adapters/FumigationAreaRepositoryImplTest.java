package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.events.FumigationAreaDelimitedEvent;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.ParcelPersistenceEntity;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@Import({FumigationAreaRepositoryImpl.class, DomainEventPublisher.class})
@RecordApplicationEvents
class FumigationAreaRepositoryImplTest extends AbstractFieldPersistenceTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final FieldGeometry PARCEL_GEOMETRY =
            FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, 0.01));

    @Autowired
    private FumigationAreaRepositoryImpl fumigationAreaRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesNewAreaAndPublishesDelimitedEvent() {
        var parcelId = persistParcel();
        var area = FumigationArea.delimit(parcelId, PARCEL_GEOMETRY, geometry(-77.041, -12.045, 0.002));

        var saved = fumigationAreaRepository.save(area);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getParcelId()).isEqualTo(parcelId);
        assertThat(applicationEvents.stream(FumigationAreaDelimitedEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.fumigationAreaId()).isEqualTo(saved.getId());
                    assertThat(event.parcelId()).isEqualTo(parcelId);
                    assertThat(event.areaHectares()).isPositive();
                });
    }

    @Test
    void findsAreaByIdWithItsGeometry() {
        var parcelId = persistParcel();
        var saved = fumigationAreaRepository.save(
                FumigationArea.delimit(parcelId, PARCEL_GEOMETRY, geometry(-77.041, -12.045, 0.002)));

        var area = fumigationAreaRepository.findById(saved.getId());

        assertThat(area).isPresent();
        assertThat(area.get().getParcelId()).isEqualTo(parcelId);
        assertThat(area.get().getGeometry().areaHectares())
                .isEqualByComparingTo(saved.getGeometry().areaHectares());
        assertThat(area.get().getGeometry().polygon().getSRID()).isEqualTo(4326);
    }

    @Test
    void redefiningAreaPublishesUpdatedEvent() {
        var parcelId = persistParcel();
        var saved = fumigationAreaRepository.save(
                FumigationArea.delimit(parcelId, PARCEL_GEOMETRY, geometry(-77.041, -12.045, 0.002)));
        var area = fumigationAreaRepository.findById(saved.getId()).orElseThrow();
        var newGeometry = geometry(-77.041, -12.045, 0.003);

        area.redefine(PARCEL_GEOMETRY, newGeometry);
        var updated = fumigationAreaRepository.save(area);

        assertThat(updated.getGeometry().areaHectares()).isEqualTo(newGeometry.areaHectares());
        assertThat(applicationEvents.stream(FumigationAreaDelimitedEvent.class)
                .filter(event -> event.areaHectares().compareTo(newGeometry.areaHectares()) == 0))
                .singleElement()
                .satisfies(event -> assertThat(event.fumigationAreaId()).isEqualTo(saved.getId()));
    }

    @Test
    void listsAreasOfParcelOrderedById() {
        var parcelId = persistParcel();
        var otherParcelId = persistParcel();
        var first = fumigationAreaRepository.save(
                FumigationArea.delimit(parcelId, PARCEL_GEOMETRY, geometry(-77.041, -12.045, 0.002)));
        var second = fumigationAreaRepository.save(
                FumigationArea.delimit(parcelId, PARCEL_GEOMETRY, geometry(-77.040, -12.044, 0.002)));
        fumigationAreaRepository.save(
                FumigationArea.delimit(otherParcelId, PARCEL_GEOMETRY, geometry(-77.041, -12.045, 0.002)));

        var areas = fumigationAreaRepository.findByParcelId(parcelId);

        assertThat(areas).extracting(FumigationArea::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void reportsExistence() {
        var parcelId = persistParcel();
        var saved = fumigationAreaRepository.save(
                FumigationArea.delimit(parcelId, PARCEL_GEOMETRY, geometry(-77.041, -12.045, 0.002)));

        assertThat(fumigationAreaRepository.existsById(saved.getId())).isTrue();
        assertThat(fumigationAreaRepository.existsById(999_999L)).isFalse();
    }

    private Long persistParcel() {
        var index = SEQUENCE.incrementAndGet();
        var farm = entityManager.persistFlushFind(new FarmPersistenceEntity(
                400L + index, "Finca " + index, "Lima", "Lima"));
        var parcel = entityManager.persistFlushFind(new ParcelPersistenceEntity(
                farm.getId(), null, "Lote " + index,
                PARCEL_GEOMETRY.polygon(), PARCEL_GEOMETRY.areaHectares(),
                ParcelStatus.ACTIVE, null));
        return parcel.getId();
    }

    private static FieldGeometry geometry(double minLongitude, double minLatitude, double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(minLongitude, minLatitude, size));
    }
}
