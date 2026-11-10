package pe.com.agridron.analytics.application.commandservices;

import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.commands.RegisterMissionHistoryCommand;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

public interface MissionHistoryCommandService {

    Result<MissionHistory, ApplicationError> handle(RegisterMissionHistoryCommand command);
}
