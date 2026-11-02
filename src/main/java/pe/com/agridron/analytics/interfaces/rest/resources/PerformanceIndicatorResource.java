package pe.com.agridron.analytics.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

public record PerformanceIndicatorResource(
        Long id,
        String name,
        BigDecimal value,
        String unit,
        String period,
        Instant calculatedAt) {
}
