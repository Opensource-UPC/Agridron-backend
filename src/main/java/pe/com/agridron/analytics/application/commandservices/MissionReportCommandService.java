package pe.com.agridron.analytics.application.commandservices;

import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.commands.AddOperationalMetricCommand;
import pe.com.agridron.analytics.domain.model.commands.DeleteMissionReportCommand;
import pe.com.agridron.analytics.domain.model.commands.GenerateMissionReportCommand;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

public interface MissionReportCommandService {

    Result<MissionReport, ApplicationError> handle(GenerateMissionReportCommand command);

    Result<MissionReport, ApplicationError> handle(AddOperationalMetricCommand command);

    Result<Long, ApplicationError> handle(DeleteMissionReportCommand command);
}
