package pe.com.agridron.analytics.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.analytics.application.queryservices.MissionReportQueryService;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.queries.GetMissionReportByIdQuery;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.repositories.MissionReportRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

@Service
public class MissionReportQueryServiceImpl implements MissionReportQueryService {

    private final MissionReportRepository missionReportRepository;

    public MissionReportQueryServiceImpl(MissionReportRepository missionReportRepository) {
        this.missionReportRepository = missionReportRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<MissionReport, ApplicationError> handle(GetMissionReportByIdQuery query) {
        if (query.reportId() == null) {
            return Result.failure(ApplicationError.notFound("Report", "null"));
        }
        return missionReportRepository.findById(query.reportId())
                .<Result<MissionReport, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("Report", String.valueOf(query.reportId()))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionReport> handle(MissionReportSearchCriteria criteria) {
        return missionReportRepository.search(criteria);
    }
}
