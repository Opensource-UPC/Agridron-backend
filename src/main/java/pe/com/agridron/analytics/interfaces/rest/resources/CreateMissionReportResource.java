package pe.com.agridron.analytics.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;

import java.math.BigDecimal;

public record CreateMissionReportResource(
        @NotNull @Positive Long missionId,
        @NotNull @Positive Long parcelId,
        @NotNull ReportType type,
        @NotNull @PositiveOrZero BigDecimal treatedArea,
        @NotNull @PositiveOrZero BigDecimal appliedVolume,
        @Size(max = 2000) String observations) {
}
