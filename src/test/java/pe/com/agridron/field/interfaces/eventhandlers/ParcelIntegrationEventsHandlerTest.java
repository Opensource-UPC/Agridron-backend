package pe.com.agridron.field.interfaces.eventhandlers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pe.com.agridron.field.domain.events.ParcelCropInformationRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelDetailsUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelGeometryUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelStatusChangedEvent;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;
import pe.com.agridron.field.interfaces.events.ParcelCropInformationRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelDetailsUpdatedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelGeometryUpdatedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelStatusChangedIntegrationEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;
import pe.com.agridron.shared.infrastructure.events.IntegrationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ParcelIntegrationEventsHandlerTest {

    private final IntegrationEventPublisher publisher = mock(IntegrationEventPublisher.class);
    private final ParcelIntegrationEventsHandler handler = new ParcelIntegrationEventsHandler(publisher);

    @Test
    void publishesParcelRegisteredIntegrationEvent() {
        handler.on(new ParcelRegisteredEvent(5L, 1L, new BigDecimal("2.50")));

        var event = capturePublished();
        assertThat(event).isInstanceOfSatisfying(
                ParcelRegisteredIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.parcelId()).isEqualTo(5L);
                    assertThat(integrationEvent.farmId()).isEqualTo(1L);
                    assertThat(integrationEvent.areaHectares()).isEqualByComparingTo("2.50");
                });
    }

    @Test
    void publishesParcelDetailsUpdatedIntegrationEvent() {
        handler.on(new ParcelDetailsUpdatedEvent(5L, 1L, "Lote 1 Actualizado"));

        var event = capturePublished();
        assertThat(event).isInstanceOfSatisfying(
                ParcelDetailsUpdatedIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.parcelId()).isEqualTo(5L);
                    assertThat(integrationEvent.farmId()).isEqualTo(1L);
                    assertThat(integrationEvent.name()).isEqualTo("Lote 1 Actualizado");
                });
    }

    @Test
    void publishesParcelGeometryUpdatedIntegrationEvent() {
        handler.on(new ParcelGeometryUpdatedEvent(5L, 1L, new BigDecimal("3.10")));

        var event = capturePublished();
        assertThat(event).isInstanceOfSatisfying(
                ParcelGeometryUpdatedIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.parcelId()).isEqualTo(5L);
                    assertThat(integrationEvent.farmId()).isEqualTo(1L);
                    assertThat(integrationEvent.areaHectares()).isEqualByComparingTo("3.10");
                });
    }

    @Test
    void publishesParcelStatusChangedIntegrationEvent() {
        handler.on(new ParcelStatusChangedEvent(5L, 1L, ParcelStatus.INACTIVE));

        var event = capturePublished();
        assertThat(event).isInstanceOfSatisfying(
                ParcelStatusChangedIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.parcelId()).isEqualTo(5L);
                    assertThat(integrationEvent.farmId()).isEqualTo(1L);
                    assertThat(integrationEvent.status()).isEqualTo("INACTIVE");
                });
    }

    @Test
    void publishesParcelCropInformationRegisteredIntegrationEvent() {
        handler.on(new ParcelCropInformationRegisteredEvent(
                5L, 1L, 2L, PhenologicalStage.FLOWERING));

        var event = capturePublished();
        assertThat(event).isInstanceOfSatisfying(
                ParcelCropInformationRegisteredIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.parcelId()).isEqualTo(5L);
                    assertThat(integrationEvent.farmId()).isEqualTo(1L);
                    assertThat(integrationEvent.cropId()).isEqualTo(2L);
                    assertThat(integrationEvent.phenologicalStage()).isEqualTo("FLOWERING");
                });
    }

    private IntegrationEvent capturePublished() {
        var captor = ArgumentCaptor.forClass(IntegrationEvent.class);
        verify(publisher).publish(captor.capture());
        verifyNoMoreInteractions(publisher);
        return captor.getValue();
    }
}
