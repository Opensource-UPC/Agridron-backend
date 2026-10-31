package pe.com.agridron.analytics.domain.model.queries;

/**
 * Query for performance indicators. A {@code null} period returns every indicator.
 */
public record GetPerformanceIndicatorsByPeriodQuery(String period) {
}
