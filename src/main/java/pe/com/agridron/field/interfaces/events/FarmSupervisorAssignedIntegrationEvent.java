package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.FarmSupervisorAssignedEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when a supervisor is assigned to a farm.
 */
public record FarmSupervisorAssignedIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long farmId,
        Long supervisorId) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.farm.supervisor-assigned";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static FarmSupervisorAssignedIntegrationEvent from(FarmSupervisorAssignedEvent event) {
        return new FarmSupervisorAssignedIntegrationEvent(
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
