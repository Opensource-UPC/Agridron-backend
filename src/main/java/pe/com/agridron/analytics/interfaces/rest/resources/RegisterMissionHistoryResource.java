package pe.com.agridron.analytics.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;

import java.time.Instant;

public record RegisterMissionHistoryResource(
        @NotNull @Positive Long missionId,
        @NotNull @Positive Long parcelId,
        @NotNull MissionFinalStatus finalStatus,
        @NotNull Instant completedAt) {
}
