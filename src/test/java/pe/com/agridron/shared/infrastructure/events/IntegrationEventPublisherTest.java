package pe.com.agridron.shared.infrastructure.events;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class IntegrationEventPublisherTest {

    private final ApplicationEventPublisher applicationEventPublisher =
            mock(ApplicationEventPublisher.class);
    private final IntegrationEventPublisher publisher =
            new IntegrationEventPublisher(applicationEventPublisher);

    @Test
    void publishesTheIntegrationEvent() {
        var event = new TestIntegrationEvent("event-1", Instant.parse("2026-10-30T10:00:00Z"));

        publisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
        verifyNoMoreInteractions(applicationEventPublisher);
    }

    private record TestIntegrationEvent(String eventId, Instant occurredOn) implements IntegrationEvent {

        @Override
        public String eventType() {
            return "test.integration-event";
        }
    }
}
