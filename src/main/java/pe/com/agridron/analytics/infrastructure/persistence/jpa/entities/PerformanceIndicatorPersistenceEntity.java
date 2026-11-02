package pe.com.agridron.analytics.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "performance_indicator")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceIndicatorPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "value", nullable = false, precision = 14, scale = 2)
    private BigDecimal value;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(nullable = false, length = 7)
    private String period;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    public PerformanceIndicatorPersistenceEntity(
            String name, BigDecimal value, String unit, String period, Instant calculatedAt) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.period = period;
        this.calculatedAt = calculatedAt;
    }

    public void update(BigDecimal value, Instant calculatedAt) {
        this.value = value;
        this.calculatedAt = calculatedAt;
    }
}
