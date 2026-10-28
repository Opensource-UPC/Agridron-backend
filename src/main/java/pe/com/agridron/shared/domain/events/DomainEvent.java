package pe.com.agridron.shared.domain.events;

/**
 * Marker interface for domain events.
 *
 * <p>Domain events describe something that already happened inside a bounded context.
 * They are registered on an aggregate root and published after the aggregate is persisted.
 * Domain events must never be consumed by other bounded contexts directly; translate them
 * into {@link IntegrationEvent}s instead.</p>
 */
public interface DomainEvent {
}
