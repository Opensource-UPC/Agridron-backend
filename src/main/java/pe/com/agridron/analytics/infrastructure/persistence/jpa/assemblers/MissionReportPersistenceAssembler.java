package pe.com.agridron.analytics.infrastructure.persistence.jpa.assemblers;

import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.entities.OperationalMetric;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.MissionReportPersistenceEntity;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.OperationalMetricPersistenceEntity;

public final class MissionReportPersistenceAssembler {

    private MissionReportPersistenceAssembler() {
    }

    public static MissionReport toDomainFromPersistence(MissionReportPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        var metrics = entity.getMetrics().stream()
                .map(metric -> OperationalMetric.reconstitute(
                        metric.getId(), metric.getName(), metric.getValue(), metric.getUnit()))
                .toList();
        return MissionReport.reconstitute(
                entity.getId(),
                entity.getMissionId(),
                entity.getParcelId(),
                entity.getType(),
                entity.getTreatedArea(),
                entity.getAppliedVolume(),
                entity.getObservations(),
                entity.getGeneratedAt(),
                metrics);
    }

    public static MissionReportPersistenceEntity toPersistenceFromDomain(MissionReport report) {
        if (report == null) {
            return null;
        }
        var entity = new MissionReportPersistenceEntity(
                report.getMissionId(),
                report.getParcelId(),
                report.getType(),
                report.getTreatedArea(),
                report.getAppliedVolume(),
                report.getObservations(),
                report.getGeneratedAt());
        entity.setId(report.getId());
        report.getMetrics().forEach(metric -> entity.addMetric(toPersistence(metric)));
        return entity;
    }

    /**
     * Adds to an existing entity the metrics that the aggregate registered but are not persisted yet.
     */
    public static void copyNewMetricsToPersistence(MissionReport report, MissionReportPersistenceEntity entity) {
        report.getMetrics().stream()
                .filter(metric -> metric.getId() == null)
                .forEach(metric -> entity.addMetric(toPersistence(metric)));
    }

    private static OperationalMetricPersistenceEntity toPersistence(OperationalMetric metric) {
        return new OperationalMetricPersistenceEntity(metric.getName(), metric.getValue(), metric.getUnit());
    }
}
