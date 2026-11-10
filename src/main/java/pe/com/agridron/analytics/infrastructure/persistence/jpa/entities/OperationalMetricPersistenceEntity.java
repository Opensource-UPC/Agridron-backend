package pe.com.agridron.analytics.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "operational_metric")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationalMetricPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_report_id", nullable = false)
    private MissionReportPersistenceEntity missionReport;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "value", nullable = false, precision = 14, scale = 3)
    private BigDecimal value;

    @Column(nullable = false, length = 30)
    private String unit;

    public OperationalMetricPersistenceEntity(String name, BigDecimal value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    void attachTo(MissionReportPersistenceEntity missionReport) {
        this.missionReport = missionReport;
    }
}
