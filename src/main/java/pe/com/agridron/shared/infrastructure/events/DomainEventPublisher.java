package pe.com.agridron.shared.infrastructure.events;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

/**
 * Shared infrastructure service responsible for publishing and clearing domain events
 * registered on {@link AbstractDomainAggregateRoot} instances.
 *
 * <p>Repository adapters should call {@link #publishAndClearEvents(AbstractDomainAggregateRoot)}
 * after persisting an aggregate. Command services must be annotated with
 * {@code @Transactional} so events are published within the same transaction.</p>
 */
@Component
@NullMarked
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Dispatches all domain events registered on the aggregate root through the Spring
     * application event publisher and clears the aggregate's internal event collection.
     *
     * @param aggregate the domain aggregate root whose events are to be published; may be {@code null}
     * @param <T>       the aggregate root type
     */
    public <T extends AbstractDomainAggregateRoot<T>> void publishAndClearEvents(@Nullable T aggregate) {
        if (aggregate == null) {
            return;
        }
        aggregate.domainEvents().forEach(applicationEventPublisher::publishEvent);
        aggregate.clearDomainEvents();
    }
}
