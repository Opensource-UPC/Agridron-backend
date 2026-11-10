package pe.com.agridron.analytics.domain.model.commands;

import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;

import java.math.BigDecimal;
import java.time.Instant;

public record GenerateMissionReportCommand(
        Long missionId,
        Long parcelId,
        ReportType type,
        BigDecimal treatedArea,
        BigDecimal appliedVolume,
        String observations,
        Instant generatedAt) {
}
