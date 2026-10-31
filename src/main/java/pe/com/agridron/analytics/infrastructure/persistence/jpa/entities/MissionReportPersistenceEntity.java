package pe.com.agridron.analytics.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "mission_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionReportPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "mission_id", nullable = false)
    private Long missionId;

    @Column(name = "parcel_id", nullable = false)
    private Long parcelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportType type;

    @Column(name = "treated_area", nullable = false, precision = 12, scale = 2)
    private BigDecimal treatedArea;

    @Column(name = "applied_volume", nullable = false, precision = 12, scale = 2)
    private BigDecimal appliedVolume;

    @Column(nullable = false, columnDefinition = "text")
    private String observations;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @OneToMany(mappedBy = "missionReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OperationalMetricPersistenceEntity> metrics = new ArrayList<>();

    public MissionReportPersistenceEntity(
            Long missionId,
            Long parcelId,
            ReportType type,
            BigDecimal treatedArea,
            BigDecimal appliedVolume,
            String observations,
            Instant generatedAt) {
        this.missionId = missionId;
        this.parcelId = parcelId;
        this.type = type;
        this.treatedArea = treatedArea;
        this.appliedVolume = appliedVolume;
        this.observations = observations;
        this.generatedAt = generatedAt;
    }

    public void addMetric(OperationalMetricPersistenceEntity metric) {
        metric.attachTo(this);
        metrics.add(metric);
    }

    public List<OperationalMetricPersistenceEntity> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }
}
