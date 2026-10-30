package pe.com.agridron.shared.infrastructure.events;

import org.jspecify.annotations.NullMarked;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

/**
 * Publishes {@link IntegrationEvent} instances through Spring's application event mechanism.
 *
 * <p>Integration events are the only supported communication mechanism between bounded
 * contexts. Producers translate their internal domain events into integration events and
 * publish them after the transaction commits, so consumers never observe rolled-back state.</p>
 */
@Component
@NullMarked
public class IntegrationEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public IntegrationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Publishes an integration event to every registered listener.
     *
     * @param event the integration event to publish
     */
    public void publish(IntegrationEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
