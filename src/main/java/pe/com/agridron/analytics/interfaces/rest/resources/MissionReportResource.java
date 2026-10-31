package pe.com.agridron.analytics.interfaces.rest.resources;

import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MissionReportResource(
        Long id,
        Long missionId,
        Long parcelId,
        ReportType type,
        BigDecimal treatedArea,
        BigDecimal appliedVolume,
        String observations,
        Instant generatedAt,
        List<OperationalMetricResource> metrics) {
}
