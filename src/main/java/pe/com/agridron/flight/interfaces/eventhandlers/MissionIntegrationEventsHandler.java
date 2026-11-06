package pe.com.agridron.flight.interfaces.eventhandlers;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.com.agridron.flight.domain.events.IncidentReportedEvent;
import pe.com.agridron.flight.domain.events.MissionCompletedEvent;
import pe.com.agridron.flight.domain.events.MissionPlannedEvent;
import pe.com.agridron.flight.domain.events.MissionStatusChangedEvent;
import pe.com.agridron.flight.interfaces.events.IncidentReportedIntegrationEvent;
import pe.com.agridron.flight.interfaces.events.MissionCompletedIntegrationEvent;
import pe.com.agridron.flight.interfaces.events.MissionPlannedIntegrationEvent;
import pe.com.agridron.flight.interfaces.events.MissionStatusChangedIntegrationEvent;
import pe.com.agridron.shared.infrastructure.events.IntegrationEventPublisher;

/**
 * Translates mission domain events into integration events.
 *
 * <p>The translation runs after the transaction commits, so other bounded contexts never
 * observe changes that were rolled back (ADR-009).</p>
 */
@Component
public class MissionIntegrationEventsHandler {

    private final IntegrationEventPublisher integrationEventPublisher;

    public MissionIntegrationEventsHandler(IntegrationEventPublisher integrationEventPublisher) {
        this.integrationEventPublisher = integrationEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MissionPlannedEvent event) {
        integrationEventPublisher.publish(MissionPlannedIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MissionStatusChangedEvent event) {
        integrationEventPublisher.publish(MissionStatusChangedIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MissionCompletedEvent event) {
        integrationEventPublisher.publish(MissionCompletedIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(IncidentReportedEvent event) {
        integrationEventPublisher.publish(IncidentReportedIntegrationEvent.from(event));
    }
}
