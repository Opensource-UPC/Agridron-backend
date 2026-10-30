package pe.com.agridron.field.interfaces.events;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.events.ParcelCropInformationRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelDetailsUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelGeometryUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelStatusChangedEvent;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ParcelIntegrationEventsTest {

    @Test
    void translatesParcelRegistered() {
        var domainEvent = new ParcelRegisteredEvent(5L, 1L, new BigDecimal("2.50"));

        var event = ParcelRegisteredIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.parcel.registered");
        assertThat(event.parcelId()).isEqualTo(5L);
        assertThat(event.farmId()).isEqualTo(1L);
        assertThat(event.areaHectares()).isEqualByComparingTo("2.50");
    }

    @Test
    void translatesParcelDetailsUpdated() {
        var domainEvent = new ParcelDetailsUpdatedEvent(5L, 1L, "Lote 1 Actualizado");

        var event = ParcelDetailsUpdatedIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.parcel.details-updated");
        assertThat(event.parcelId()).isEqualTo(5L);
        assertThat(event.farmId()).isEqualTo(1L);
        assertThat(event.name()).isEqualTo("Lote 1 Actualizado");
    }

    @Test
    void translatesParcelGeometryUpdated() {
        var domainEvent = new ParcelGeometryUpdatedEvent(5L, 1L, new BigDecimal("3.10"));

        var event = ParcelGeometryUpdatedIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.parcel.geometry-updated");
        assertThat(event.parcelId()).isEqualTo(5L);
        assertThat(event.farmId()).isEqualTo(1L);
        assertThat(event.areaHectares()).isEqualByComparingTo("3.10");
    }

    @Test
    void translatesParcelStatusChanged() {
        var domainEvent = new ParcelStatusChangedEvent(5L, 1L, ParcelStatus.INACTIVE);

        var event = ParcelStatusChangedIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.parcel.status-changed");
        assertThat(event.parcelId()).isEqualTo(5L);
        assertThat(event.farmId()).isEqualTo(1L);
        assertThat(event.status()).isEqualTo("INACTIVE");
    }

    @Test
    void translatesParcelCropInformationRegistered() {
        var domainEvent = new ParcelCropInformationRegisteredEvent(
                5L, 1L, 2L, PhenologicalStage.FLOWERING);

        var event = ParcelCropInformationRegisteredIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.parcel.crop-information-registered");
        assertThat(event.parcelId()).isEqualTo(5L);
        assertThat(event.farmId()).isEqualTo(1L);
        assertThat(event.cropId()).isEqualTo(2L);
        assertThat(event.phenologicalStage()).isEqualTo("FLOWERING");
    }

    @Test
    void translatesParcelCropInformationWithoutCrop() {
        var domainEvent = new ParcelCropInformationRegisteredEvent(5L, 1L, null, null);

        var event = ParcelCropInformationRegisteredIntegrationEvent.from(domainEvent);

        assertThat(event.cropId()).isNull();
        assertThat(event.phenologicalStage()).isNull();
    }

    @Test
    void generatesADifferentEventIdPerInstance() {
        var domainEvent = new ParcelRegisteredEvent(5L, 1L, new BigDecimal("2.50"));

        var first = ParcelRegisteredIntegrationEvent.from(domainEvent);
        var second = ParcelRegisteredIntegrationEvent.from(domainEvent);

        assertThat(first.eventId()).isNotEqualTo(second.eventId());
    }
}
