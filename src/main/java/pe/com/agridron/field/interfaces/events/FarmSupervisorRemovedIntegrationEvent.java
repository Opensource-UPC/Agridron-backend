package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.FarmSupervisorRemovedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when a supervisor is removed from a farm.
 */
public record FarmSupervisorRemovedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long farmId,
        Long supervisorId) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.farm.supervisor-removed";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static FarmSupervisorRemovedIntegrationEvent from(FarmSupervisorRemovedEvent event) {
        return new FarmSupervisorRemovedIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.farmId(),
                event.supervisorId());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
