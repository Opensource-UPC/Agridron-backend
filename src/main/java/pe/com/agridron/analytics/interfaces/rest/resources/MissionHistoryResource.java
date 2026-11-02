package pe.com.agridron.analytics.interfaces.rest.resources;

import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;

import java.time.Instant;

public record MissionHistoryResource(
        Long id,
        Long missionId,
        Long parcelId,
        MissionFinalStatus finalStatus,
        Instant completedAt) {
}
