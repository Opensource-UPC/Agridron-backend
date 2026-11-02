package pe.com.agridron.analytics.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.analytics.domain.events.PerformanceIndicatorCalculatedEvent;
import pe.com.agridron.analytics.domain.model.services.DomainValidation;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Performance indicator consolidated for a month (period in {@code YYYY-MM} format).
 * There is one indicator per name and period.
 */
@Getter
public class PerformanceIndicator extends AbstractDomainAggregateRoot<PerformanceIndicator> {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int UNIT_MAX_LENGTH = 30;
    private static final int PERIOD_LENGTH = 7;

    private Long id;
    private final String name;
    private BigDecimal value;
    private final String unit;
    private final String period;
    private Instant calculatedAt;

    private PerformanceIndicator(
            String name, BigDecimal value, String unit, String period, Instant calculatedAt) {
        this.name = DomainValidation.requireText(name, "name", NAME_MAX_LENGTH);
        this.value = DomainValidation.requireNonNegative(value, "value");
        this.unit = DomainValidation.requireText(unit, "unit", UNIT_MAX_LENGTH);
        this.period = normalizePeriod(period);
        this.calculatedAt = DomainValidation.requireNonNull(calculatedAt, "calculatedAt");
    }

    public static PerformanceIndicator calculate(
            String name, BigDecimal value, String unit, String period, Instant calculatedAt) {
        return new PerformanceIndicator(name, value, unit, period, calculatedAt);
    }

    public static PerformanceIndicator reconstitute(
            Long id, String name, BigDecimal value, String unit, String period, Instant calculatedAt) {
        var indicator = new PerformanceIndicator(name, value, unit, period, calculatedAt);
        indicator.id = DomainValidation.requirePositive(id, "id");
        return indicator;
    }

    /**
     * Normalizes a period to {@code YYYY-MM}, rejecting any other format.
     */
    public static String normalizePeriod(String period) {
        var text = DomainValidation.requireText(period, "period", PERIOD_LENGTH);
        try {
            return YearMonth.parse(text).toString();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("period must have the format YYYY-MM", ex);
        }
    }

    public void onCreated() {
        registerDomainEvent(PerformanceIndicatorCalculatedEvent.from(this));
    }

    public void recalculate(BigDecimal newValue, Instant newCalculatedAt) {
        this.value = DomainValidation.requireNonNegative(newValue, "value");
        this.calculatedAt = DomainValidation.requireNonNull(newCalculatedAt, "calculatedAt");
        registerDomainEvent(PerformanceIndicatorCalculatedEvent.from(this));
    }
}
