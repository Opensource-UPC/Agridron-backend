package pe.com.agridron.field.interfaces.eventhandlers;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.com.agridron.field.domain.events.ParcelCropInformationRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelDetailsUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelGeometryUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelStatusChangedEvent;
import pe.com.agridron.field.interfaces.events.ParcelCropInformationRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelDetailsUpdatedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelGeometryUpdatedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelStatusChangedIntegrationEvent;
import pe.com.agridron.shared.infrastructure.events.IntegrationEventPublisher;

/**
 * Translates parcel domain events into integration events.
 *
 * <p>The translation runs after the transaction commits, so other bounded contexts never
 * observe changes that were rolled back.</p>
 */
@Component
public class ParcelIntegrationEventsHandler {

    private final IntegrationEventPublisher integrationEventPublisher;

    public ParcelIntegrationEventsHandler(IntegrationEventPublisher integrationEventPublisher) {
        this.integrationEventPublisher = integrationEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ParcelRegisteredEvent event) {
        integrationEventPublisher.publish(ParcelRegisteredIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ParcelDetailsUpdatedEvent event) {
        integrationEventPublisher.publish(ParcelDetailsUpdatedIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ParcelGeometryUpdatedEvent event) {
        integrationEventPublisher.publish(ParcelGeometryUpdatedIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ParcelStatusChangedEvent event) {
        integrationEventPublisher.publish(ParcelStatusChangedIntegrationEvent.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ParcelCropInformationRegisteredEvent event) {
        integrationEventPublisher.publish(ParcelCropInformationRegisteredIntegrationEvent.from(event));
    }
}
