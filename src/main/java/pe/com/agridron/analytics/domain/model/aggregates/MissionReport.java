package pe.com.agridron.analytics.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.analytics.domain.events.MissionReportGeneratedEvent;
import pe.com.agridron.analytics.domain.model.entities.OperationalMetric;
import pe.com.agridron.analytics.domain.model.services.DomainValidation;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Report generated from a mission (summary or phytosanitary) with its operational metrics.
 * A mission can have at most one report of each type.
 */
@Getter
public class MissionReport extends AbstractDomainAggregateRoot<MissionReport> {

    private static final int OBSERVATIONS_MAX_LENGTH = 2000;

    private Long id;
    private final Long missionId;
    private final Long parcelId;
    private final ReportType type;
    private final BigDecimal treatedArea;
    private final BigDecimal appliedVolume;
    private final String observations;
    private final Instant generatedAt;
    private final List<OperationalMetric> metrics = new ArrayList<>();

    private MissionReport(
            Long missionId,
            Long parcelId,
            ReportType type,
            BigDecimal treatedArea,
            BigDecimal appliedVolume,
            String observations,
            Instant generatedAt) {
        this.missionId = DomainValidation.requirePositive(missionId, "missionId");
        this.parcelId = DomainValidation.requirePositive(parcelId, "parcelId");
        this.type = DomainValidation.requireNonNull(type, "type");
        this.treatedArea = DomainValidation.requireNonNegative(treatedArea, "treatedArea");
        this.appliedVolume = DomainValidation.requireNonNegative(appliedVolume, "appliedVolume");
        this.observations = DomainValidation.optionalText(observations, "observations", OBSERVATIONS_MAX_LENGTH);
        this.generatedAt = DomainValidation.requireNonNull(generatedAt, "generatedAt");
    }

    public static MissionReport generate(
            Long missionId,
            Long parcelId,
            ReportType type,
            BigDecimal treatedArea,
            BigDecimal appliedVolume,
            String observations,
            Instant generatedAt) {
        return new MissionReport(missionId, parcelId, type, treatedArea, appliedVolume, observations, generatedAt);
    }

    public static MissionReport reconstitute(
            Long id,
            Long missionId,
            Long parcelId,
            ReportType type,
            BigDecimal treatedArea,
            BigDecimal appliedVolume,
            String observations,
            Instant generatedAt,
            Collection<OperationalMetric> metrics) {
        var report = new MissionReport(
                missionId, parcelId, type, treatedArea, appliedVolume, observations, generatedAt);
        report.id = DomainValidation.requirePositive(id, "id");
        if (metrics != null) {
            report.metrics.addAll(metrics);
        }
        return report;
    }

    public void onCreated() {
        registerDomainEvent(MissionReportGeneratedEvent.from(this));
    }

    public boolean hasMetric(String name) {
        if (name == null) {
            return false;
        }
        var normalized = name.trim();
        return metrics.stream().anyMatch(metric -> metric.getName().equalsIgnoreCase(normalized));
    }

    public OperationalMetric addMetric(String name, BigDecimal value, String unit) {
        var metric = OperationalMetric.register(name, value, unit);
        if (hasMetric(metric.getName())) {
            throw new IllegalArgumentException("metric %s already exists".formatted(metric.getName()));
        }
        metrics.add(metric);
        return metric;
    }

    /**
     * Liters applied per treated hectare, or {@code null} when no area was treated.
     */
    public BigDecimal applicationRate() {
        if (treatedArea.signum() == 0) {
            return null;
        }
        return appliedVolume.divide(treatedArea, 2, RoundingMode.HALF_UP);
    }

    public List<OperationalMetric> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }
}
