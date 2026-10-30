package pe.com.agridron.field.interfaces.events;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.events.FumigationAreaDelimitedEvent;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FumigationAreaDelimitedIntegrationEventTest {

    @Test
    void translatesFumigationAreaDelimited() {
        var domainEvent = new FumigationAreaDelimitedEvent(7L, 5L, new BigDecimal("1.25"));

        var event = FumigationAreaDelimitedIntegrationEvent.from(domainEvent);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredOn()).isNotNull();
        assertThat(event.eventType()).isEqualTo("field.fumigation-area.delimited");
        assertThat(event.fumigationAreaId()).isEqualTo(7L);
        assertThat(event.parcelId()).isEqualTo(5L);
        assertThat(event.areaHectares()).isEqualByComparingTo("1.25");
    }

    @Test
    void generatesADifferentEventIdPerInstance() {
        var domainEvent = new FumigationAreaDelimitedEvent(7L, 5L, new BigDecimal("1.25"));

        var first = FumigationAreaDelimitedIntegrationEvent.from(domainEvent);
        var second = FumigationAreaDelimitedIntegrationEvent.from(domainEvent);

        assertThat(first.eventId()).isNotEqualTo(second.eventId());
    }
}
