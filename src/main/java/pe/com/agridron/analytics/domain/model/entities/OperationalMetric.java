package pe.com.agridron.analytics.domain.model.entities;

import lombok.Getter;
import pe.com.agridron.analytics.domain.model.services.DomainValidation;

import java.math.BigDecimal;

/**
 * Measurable value (name, value, unit) that belongs to a mission report.
 */
@Getter
public class OperationalMetric {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int UNIT_MAX_LENGTH = 30;

    private Long id;
    private final String name;
    private final BigDecimal value;
    private final String unit;

    private OperationalMetric(String name, BigDecimal value, String unit) {
        this.name = DomainValidation.requireText(name, "name", NAME_MAX_LENGTH);
        this.value = DomainValidation.requireValue(value, "value");
        this.unit = DomainValidation.requireText(unit, "unit", UNIT_MAX_LENGTH);
    }

    public static OperationalMetric register(String name, BigDecimal value, String unit) {
        return new OperationalMetric(name, value, unit);
    }

    public static OperationalMetric reconstitute(Long id, String name, BigDecimal value, String unit) {
        var metric = new OperationalMetric(name, value, unit);
        metric.id = DomainValidation.requirePositive(id, "id");
        return metric;
    }
}
