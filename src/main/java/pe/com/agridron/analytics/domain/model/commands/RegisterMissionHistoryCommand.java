package pe.com.agridron.analytics.domain.model.commands;

import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;

import java.time.Instant;

public record RegisterMissionHistoryCommand(
        Long missionId,
        Long parcelId,
        MissionFinalStatus finalStatus,
        Instant completedAt) {
}
