package pe.com.agridron.field.interfaces.eventhandlers;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.com.agridron.field.domain.events.FumigationAreaDelimitedEvent;
import pe.com.agridron.field.interfaces.events.FumigationAreaDelimitedIntegrationEvent;
import pe.com.agridron.shared.infrastructure.events.IntegrationEventPublisher;

/**
 * Translates fumigation area domain events into integration events.
 *
 * <p>The translation runs after the transaction commits, so other bounded contexts never
 * observe changes that were rolled back.</p>
 */
@Component
public class FumigationAreaIntegrationEventsHandler {

    private final IntegrationEventPublisher integrationEventPublisher;

    public FumigationAreaIntegrationEventsHandler(IntegrationEventPublisher integrationEventPublisher) {
        this.integrationEventPublisher = integrationEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FumigationAreaDelimitedEvent event) {
        integrationEventPublisher.publish(FumigationAreaDelimitedIntegrationEvent.from(event));
    }
}
