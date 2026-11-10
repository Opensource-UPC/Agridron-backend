package pe.com.agridron.analytics.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.analytics.application.queryservices.MissionHistoryQueryService;
import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.queries.GetMissionHistoryByIdQuery;
import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;
import pe.com.agridron.analytics.domain.repositories.MissionHistoryRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

@Service
public class MissionHistoryQueryServiceImpl implements MissionHistoryQueryService {

    private final MissionHistoryRepository missionHistoryRepository;

    public MissionHistoryQueryServiceImpl(MissionHistoryRepository missionHistoryRepository) {
        this.missionHistoryRepository = missionHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<MissionHistory, ApplicationError> handle(GetMissionHistoryByIdQuery query) {
        if (query.historyId() == null) {
            return Result.failure(ApplicationError.notFound("History", "null"));
        }
        return missionHistoryRepository.findById(query.historyId())
                .<Result<MissionHistory, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("History", String.valueOf(query.historyId()))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionHistory> handle(MissionHistorySearchCriteria criteria) {
        return missionHistoryRepository.search(criteria);
    }
}
