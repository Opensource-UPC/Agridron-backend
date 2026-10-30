package pe.com.agridron.field.interfaces.eventhandlers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pe.com.agridron.field.domain.events.FarmRegisteredEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorAssignedEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorRemovedEvent;
import pe.com.agridron.field.interfaces.events.FarmRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.FarmSupervisorAssignedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.FarmSupervisorRemovedIntegrationEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;
import pe.com.agridron.shared.infrastructure.events.IntegrationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class FarmIntegrationEventsHandlerTest {

    private final IntegrationEventPublisher publisher = mock(IntegrationEventPublisher.class);
    private final FarmIntegrationEventsHandler handler = new FarmIntegrationEventsHandler(publisher);

    @Test
    void publishesFarmRegisteredIntegrationEvent() {
        handler.on(new FarmRegisteredEvent(1L, 10L, "Finca El Sol", "Lima", "Lima"));

        var event = capturePublished();
        assertThat(event).isInstanceOfSatisfying(
                FarmRegisteredIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.farmId()).isEqualTo(1L);
                    assertThat(integrationEvent.ownerId()).isEqualTo(10L);
                    assertThat(integrationEvent.name()).isEqualTo("Finca El Sol");
                });
    }

    @Test
    void publishesSupervisorAssignedIntegrationEvent() {
        handler.on(new FarmSupervisorAssignedEvent(1L, 20L));

        var event = capturePublished();
        assertThat(event).isInstanceOfSatisfying(
                FarmSupervisorAssignedIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.farmId()).isEqualTo(1L);
                    assertThat(integrationEvent.supervisorId()).isEqualTo(20L);
                });
    }

    @Test
    void publishesSupervisorRemovedIntegrationEvent() {
        handler.on(new FarmSupervisorRemovedEvent(1L, 20L));

        var event = capturePublished();
        assertThat(event).isInstanceOfSatisfying(
                FarmSupervisorRemovedIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.farmId()).isEqualTo(1L);
                    assertThat(integrationEvent.supervisorId()).isEqualTo(20L);
                });
    }

    private IntegrationEvent capturePublished() {
        var captor = ArgumentCaptor.forClass(IntegrationEvent.class);
        verify(publisher).publish(captor.capture());
        verifyNoMoreInteractions(publisher);
        return captor.getValue();
    }
}
