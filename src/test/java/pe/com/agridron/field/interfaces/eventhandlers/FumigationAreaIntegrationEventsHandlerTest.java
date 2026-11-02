package pe.com.agridron.field.interfaces.eventhandlers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pe.com.agridron.field.domain.events.FumigationAreaDelimitedEvent;
import pe.com.agridron.field.interfaces.events.FumigationAreaDelimitedIntegrationEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;
import pe.com.agridron.shared.infrastructure.events.IntegrationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class FumigationAreaIntegrationEventsHandlerTest {

    private final IntegrationEventPublisher publisher = mock(IntegrationEventPublisher.class);
    private final FumigationAreaIntegrationEventsHandler handler =
            new FumigationAreaIntegrationEventsHandler(publisher);

    @Test
    void publishesFumigationAreaDelimitedIntegrationEvent() {
        handler.on(new FumigationAreaDelimitedEvent(7L, 5L, new BigDecimal("1.25")));

        var captor = ArgumentCaptor.forClass(IntegrationEvent.class);
        verify(publisher).publish(captor.capture());
        verifyNoMoreInteractions(publisher);

        assertThat(captor.getValue()).isInstanceOfSatisfying(
                FumigationAreaDelimitedIntegrationEvent.class, integrationEvent -> {
                    assertThat(integrationEvent.fumigationAreaId()).isEqualTo(7L);
                    assertThat(integrationEvent.parcelId()).isEqualTo(5L);
                    assertThat(integrationEvent.areaHectares()).isEqualByComparingTo("1.25");
                });
    }
}
