package pe.com.agridron.field.interfaces.eventhandlers;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.com.agridron.field.domain.events.FarmRegisteredEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorAssignedEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorRemovedEvent;
import pe.com.agridron.field.interfaces.events.FarmRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.FarmSupervisorAssignedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.FarmSupervisorRemovedIntegrationEvent;
import pe.com.agridron.shared.infrastructure.events.IntegrationEventPublisher;

/**
 * Translates farm domain events into integration events.
 *
 * <p>The translation runs after the transaction commits, so other bounded contexts never
 * observe changes that were rolled back. This handler is an interface adapter: it reads
 * internal domain events and publishes the Field published language.</p>
 */
@Component
public class FarmIntegrationEventsHandler {

    private final IntegrationEventPublisher integrationEventPublisher;

    public FarmIntegrationEventsHandler(IntegrationEventPublisher integrationEventPublisher) {
        this.integrationEventPublisher = integrationEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FarmRegisteredEvent event) {
        integrationEventPublisher.publish(FarmRegisteredIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FarmSupervisorAssignedEvent event) {
        integrationEventPublisher.publish(FarmSupervisorAssignedIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FarmSupervisorRemovedEvent event) {
        integrationEventPublisher.publish(FarmSupervisorRemovedIntegrationEvent.from(event));
    }
}
