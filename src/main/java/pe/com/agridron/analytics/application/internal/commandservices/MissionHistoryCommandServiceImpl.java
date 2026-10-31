package pe.com.agridron.analytics.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.analytics.application.commandservices.MissionHistoryCommandService;
import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.commands.RegisterMissionHistoryCommand;
import pe.com.agridron.analytics.domain.repositories.MissionHistoryRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

@Service
public class MissionHistoryCommandServiceImpl implements MissionHistoryCommandService {

    private final MissionHistoryRepository missionHistoryRepository;

    public MissionHistoryCommandServiceImpl(MissionHistoryRepository missionHistoryRepository) {
        this.missionHistoryRepository = missionHistoryRepository;
    }

    @Override
    @Transactional
    public Result<MissionHistory, ApplicationError> handle(RegisterMissionHistoryCommand command) {
        MissionHistory history;
        try {
            history = MissionHistory.record(
                    command.missionId(), command.parcelId(), command.finalStatus(), command.completedAt());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("history", ex.getMessage()));
        }
        if (missionHistoryRepository.existsByMissionId(history.getMissionId())) {
            return Result.failure(ApplicationError.conflict(
                    "History", "Mission %d is already archived".formatted(history.getMissionId())));
        }
        return Result.success(missionHistoryRepository.save(history));
    }
}
