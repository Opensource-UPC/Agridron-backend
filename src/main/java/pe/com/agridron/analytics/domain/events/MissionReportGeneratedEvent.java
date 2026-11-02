package pe.com.agridron.analytics.domain.events;

import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.shared.domain.events.DomainEvent;

public record MissionReportGeneratedEvent(
        Long reportId,
        Long missionId,
        Long parcelId,
        ReportType type) implements DomainEvent {

    public static MissionReportGeneratedEvent from(MissionReport report) {
        return new MissionReportGeneratedEvent(
                report.getId(),
                report.getMissionId(),
                report.getParcelId(),
                report.getType());
    }
}
