package pe.com.agridron.analytics.domain.model.commands;

import java.time.Instant;

public record CalculatePerformanceIndicatorsCommand(String period, Instant calculatedAt) {
}
