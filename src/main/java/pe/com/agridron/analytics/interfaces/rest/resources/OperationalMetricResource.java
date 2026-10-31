package pe.com.agridron.analytics.interfaces.rest.resources;

import java.math.BigDecimal;

public record OperationalMetricResource(Long id, String name, BigDecimal value, String unit) {
}
