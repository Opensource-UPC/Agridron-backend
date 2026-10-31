package pe.com.agridron.analytics.domain.events;

import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.shared.domain.events.DomainEvent;

import java.math.BigDecimal;

public record PerformanceIndicatorCalculatedEvent(
        Long indicatorId,
        String name,
        String period,
        BigDecimal value) implements DomainEvent {

    public static PerformanceIndicatorCalculatedEvent from(PerformanceIndicator indicator) {
        return new PerformanceIndicatorCalculatedEvent(
                indicator.getId(),
                indicator.getName(),
                indicator.getPeriod(),
                indicator.getValue());
    }
}
