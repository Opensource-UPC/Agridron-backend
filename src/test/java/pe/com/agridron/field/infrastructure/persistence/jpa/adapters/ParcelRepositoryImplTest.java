package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.events.ParcelCropInformationRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelGeometryUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelStatusChangedEvent;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.CropPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@Import({ParcelRepositoryImpl.class, DomainEventPublisher.class})
@RecordApplicationEvents
class ParcelRepositoryImplTest extends AbstractFieldPersistenceTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @Autowired
    private ParcelRepositoryImpl parcelRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesNewParcelAndPublishesCreationEvent() {
        var farmId = persistFarm();
        var parcel = Parcel.create(farmId, "Lote 1", geometry(0.001), null, null);

        var saved = parcelRepository.save(parcel);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFarmId()).isEqualTo(farmId);
        assertThat(applicationEvents.stream(ParcelRegisteredEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.parcelId()).isEqualTo(saved.getId());
                    assertThat(event.farmId()).isEqualTo(farmId);
                    assertThat(event.areaHectares()).isPositive();
                });
        assertThat(applicationEvents.stream(ParcelCropInformationRegisteredEvent.class)).isEmpty();
    }

    @Test
    void savesParcelWithCropAndPublishesBothCreationEvents() {
        var farmId = persistFarm();
        var cropId = persistCrop();
        var parcel = Parcel.create(farmId, "Lote 1", geometry(0.001), cropId, PhenologicalStage.VEGETATIVE);

        var saved = parcelRepository.save(parcel);

        assertThat(saved.getCropId()).isEqualTo(cropId);
        assertThat(applicationEvents.stream(ParcelCropInformationRegisteredEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.parcelId()).isEqualTo(saved.getId());
                    assertThat(event.cropId()).isEqualTo(cropId);
                    assertThat(event.phenologicalStage()).isEqualTo(PhenologicalStage.VEGETATIVE);
                });
    }

    @Test
    void findsParcelByIdWithItsGeometry() {
        var farmId = persistFarm();
        var saved = parcelRepository.save(Parcel.create(farmId, "Lote 1", geometry(0.001), null, null));

        var parcel = parcelRepository.findById(saved.getId());

        assertThat(parcel).isPresent();
        assertThat(parcel.get().getName()).isEqualTo("Lote 1");
        assertThat(parcel.get().getGeometry().areaHectares())
                .isEqualByComparingTo(saved.getGeometry().areaHectares());
        assertThat(parcel.get().getGeometry().polygon().getSRID()).isEqualTo(4326);
    }

    @Test
    void redefiningGeometryPublishesUpdatedArea() {
        var farmId = persistFarm();
        var saved = parcelRepository.save(Parcel.create(farmId, "Lote 1", geometry(0.001), null, null));
        var parcel = parcelRepository.findById(saved.getId()).orElseThrow();
        var newGeometry = geometry(0.003);

        parcel.redefineGeometry(newGeometry);
        var updated = parcelRepository.save(parcel);

        assertThat(updated.getGeometry().areaHectares()).isEqualTo(newGeometry.areaHectares());
        assertThat(applicationEvents.stream(ParcelGeometryUpdatedEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.parcelId()).isEqualTo(saved.getId());
                    assertThat(event.areaHectares()).isEqualTo(newGeometry.areaHectares());
                });
    }

    @Test
    void changingStatusPublishesEvent() {
        var farmId = persistFarm();
        var saved = parcelRepository.save(Parcel.create(farmId, "Lote 1", geometry(0.001), null, null));
        var parcel = parcelRepository.findById(saved.getId()).orElseThrow();

        parcel.deactivate();
        var updated = parcelRepository.save(parcel);

        assertThat(updated.getStatus()).isEqualTo(ParcelStatus.INACTIVE);
        assertThat(applicationEvents.stream(ParcelStatusChangedEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.parcelId()).isEqualTo(saved.getId());
                    assertThat(event.status()).isEqualTo(ParcelStatus.INACTIVE);
                });
    }

    @Test
    void listsParcelsOfFarmOrderedByName() {
        var farmId = persistFarm();
        var otherFarmId = persistFarm();
        parcelRepository.save(Parcel.create(farmId, "Zeta", geometry(0.001), null, null));
        parcelRepository.save(Parcel.create(farmId, "Alfa", geometry(0.001), null, null));
        parcelRepository.save(Parcel.create(otherFarmId, "Otra", geometry(0.001), null, null));

        var parcels = parcelRepository.findByFarmId(farmId);

        assertThat(parcels).extracting(Parcel::getName).containsExactly("Alfa", "Zeta");
    }

    @Test
    void searchWithoutCriteriaReturnsAllParcelsOrderedByName() {
        var farmId = persistFarm();
        parcelRepository.save(Parcel.create(farmId, "Gamma", geometry(0.003), null, null));
        parcelRepository.save(Parcel.create(farmId, "Alfa", geometry(0.001), null, null));
        parcelRepository.save(Parcel.create(farmId, "Beta", geometry(0.002), null, null));

        var result = parcelRepository.search(new ParcelSearchCriteria(null, null, null, null, null));

        assertThat(result).extracting(Parcel::getName)
                .containsExactly("Alfa", "Beta", "Gamma");
    }

    @Test
    void searchesByFarmStatusCropAndAreaRange() {
        var firstFarmId = persistFarm();
        var secondFarmId = persistFarm();
        var cropId = persistCrop();
        var small = parcelRepository.save(
                Parcel.create(firstFarmId, "Alfa", geometry(0.001), cropId, PhenologicalStage.VEGETATIVE));
        var medium = parcelRepository.save(
                Parcel.create(firstFarmId, "Beta", geometry(0.002), null, null));
        parcelRepository.save(
                Parcel.create(secondFarmId, "Gamma", geometry(0.003), cropId, PhenologicalStage.FLOWERING));
        medium.deactivate();
        parcelRepository.save(medium);

        assertThat(parcelRepository.search(new ParcelSearchCriteria(firstFarmId, null, null, null, null)))
                .extracting(Parcel::getName)
                .containsExactly("Alfa", "Beta");
        assertThat(parcelRepository.search(new ParcelSearchCriteria(null, ParcelStatus.INACTIVE, null, null, null)))
                .extracting(Parcel::getName)
                .containsExactly("Beta");
        assertThat(parcelRepository.search(new ParcelSearchCriteria(null, null, cropId, null, null)))
                .extracting(Parcel::getName)
                .containsExactly("Alfa", "Gamma");
        assertThat(parcelRepository.search(new ParcelSearchCriteria(
                null, null, null, new java.math.BigDecimal("2"), null)))
                .extracting(Parcel::getName)
                .containsExactly("Beta", "Gamma");
        assertThat(parcelRepository.search(new ParcelSearchCriteria(
                null, null, null, null, new java.math.BigDecimal("2"))))
                .extracting(Parcel::getName)
                .containsExactly("Alfa");
        assertThat(parcelRepository.search(new ParcelSearchCriteria(firstFarmId, null, cropId, null, null)))
                .extracting(Parcel::getName)
                .containsExactly("Alfa");
        assertThat(small.getId()).isNotNull();
    }

    @Test
    void searchWithoutMatchesReturnsEmptyList() {
        var farmId = persistFarm();
        parcelRepository.save(Parcel.create(farmId, "Lote 1", geometry(0.001), null, null));

        var result = parcelRepository.search(new ParcelSearchCriteria(
                null, null, null, new java.math.BigDecimal("100"), null));

        assertThat(result).isEmpty();
    }

    @Test
    void delegatesExistenceChecks() {
        var farmId = persistFarm();
        var saved = parcelRepository.save(Parcel.create(farmId, "Lote 1", geometry(0.001), null, null));

        assertThat(parcelRepository.existsByFarmIdAndName(farmId, "Lote 1")).isTrue();
        assertThat(parcelRepository.existsByFarmIdAndName(farmId, "Lote 2")).isFalse();
        assertThat(parcelRepository.existsByFarmIdAndNameAndIdNot(farmId, "Lote 1", saved.getId()))
                .isFalse();
        assertThat(parcelRepository.existsByFarmIdAndNameAndIdNot(farmId, "Lote 1", 999_999L)).isTrue();
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

    private static FieldGeometry geometry(double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, size));
    }
}
