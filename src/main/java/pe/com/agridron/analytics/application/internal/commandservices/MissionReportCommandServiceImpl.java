package pe.com.agridron.analytics.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.analytics.application.commandservices.MissionReportCommandService;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.commands.AddOperationalMetricCommand;
import pe.com.agridron.analytics.domain.model.commands.DeleteMissionReportCommand;
import pe.com.agridron.analytics.domain.model.commands.GenerateMissionReportCommand;
import pe.com.agridron.analytics.domain.repositories.MissionReportRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

@Service
public class MissionReportCommandServiceImpl implements MissionReportCommandService {

    private static final String RESOURCE = "Report";

    private final MissionReportRepository missionReportRepository;

    public MissionReportCommandServiceImpl(MissionReportRepository missionReportRepository) {
        this.missionReportRepository = missionReportRepository;
    }

    @Override
    @Transactional
    public Result<MissionReport, ApplicationError> handle(GenerateMissionReportCommand command) {
        MissionReport report;
        try {
            report = MissionReport.generate(
                    command.missionId(),
                    command.parcelId(),
                    command.type(),
                    command.treatedArea(),
                    command.appliedVolume(),
                    command.observations(),
                    command.generatedAt());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("report", ex.getMessage()));
        }
        if (missionReportRepository.existsByMissionIdAndType(report.getMissionId(), report.getType())) {
            return Result.failure(ApplicationError.conflict(
                    RESOURCE,
                    "A %s report already exists for mission %d".formatted(report.getType(), report.getMissionId())));
        }
        return Result.success(missionReportRepository.save(report));
    }

    @Override
    @Transactional
    public Result<MissionReport, ApplicationError> handle(AddOperationalMetricCommand command) {
        if (command.reportId() == null) {
            return Result.failure(ApplicationError.validationError("reportId", "reportId is required"));
        }
        var found = missionReportRepository.findById(command.reportId());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound(RESOURCE, String.valueOf(command.reportId())));
        }
        var report = found.get();
        if (report.hasMetric(command.name())) {
            return Result.failure(ApplicationError.conflict(
                    RESOURCE, "Metric %s already exists in report %d".formatted(command.name(), report.getId())));
        }
        try {
            report.addMetric(command.name(), command.value(), command.unit());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("metric", ex.getMessage()));
        }
        return Result.success(missionReportRepository.save(report));
    }

    @Override
    @Transactional
    public Result<Long, ApplicationError> handle(DeleteMissionReportCommand command) {
        if (command.reportId() == null || !missionReportRepository.existsById(command.reportId())) {
            return Result.failure(ApplicationError.notFound(RESOURCE, String.valueOf(command.reportId())));
        }
        missionReportRepository.deleteById(command.reportId());
        return Result.success(command.reportId());
    }
}
