package pe.com.agridron.analytics.domain.model.commands;

import java.math.BigDecimal;

public record AddOperationalMetricCommand(Long reportId, String name, BigDecimal value, String unit) {
}
