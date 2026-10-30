package pe.com.agridron.field.interfaces.events;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.events.FarmRegisteredEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorAssignedEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorRemovedEvent;

import static org.assertj.core.api.Assertions.assertThat;

class FarmIntegrationEventsTest {

    @Test
    void translatesFarmRegistered() {
        var domainEvent = new FarmRegisteredEvent(1L, 10L, "Finca El Sol", "Lurín, Lima", "Lima");

        var event = FarmRegisteredIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.farm.registered");
        assertThat(event.farmId()).isEqualTo(1L);
        assertThat(event.ownerId()).isEqualTo(10L);
        assertThat(event.name()).isEqualTo("Finca El Sol");
        assertThat(event.location()).isEqualTo("Lurín, Lima");
        assertThat(event.department()).isEqualTo("Lima");
    }

    @Test
    void translatesSupervisorAssigned() {
        var domainEvent = new FarmSupervisorAssignedEvent(1L, 20L);

        var event = FarmSupervisorAssignedIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.farm.supervisor-assigned");
        assertThat(event.farmId()).isEqualTo(1L);
        assertThat(event.supervisorId()).isEqualTo(20L);
    }

    @Test
    void translatesSupervisorRemoved() {
        var domainEvent = new FarmSupervisorRemovedEvent(1L, 20L);

        var event = FarmSupervisorRemovedIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.farm.supervisor-removed");
        assertThat(event.farmId()).isEqualTo(1L);
        assertThat(event.supervisorId()).isEqualTo(20L);
    }
}
