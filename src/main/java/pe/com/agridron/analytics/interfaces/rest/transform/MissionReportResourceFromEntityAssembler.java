package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.interfaces.rest.resources.MissionReportResource;

public final class MissionReportResourceFromEntityAssembler {

    private MissionReportResourceFromEntityAssembler() {
    }

    public static MissionReportResource toResourceFromEntity(MissionReport report) {
        return new MissionReportResource(
                report.getId(),
                report.getMissionId(),
                report.getParcelId(),
                report.getType(),
                report.getTreatedArea(),
                report.getAppliedVolume(),
                report.getObservations(),
                report.getGeneratedAt(),
                report.getMetrics().stream()
                        .map(OperationalMetricResourceFromEntityAssembler::toResourceFromEntity)
                        .toList());
    }
}
