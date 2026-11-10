package pe.com.agridron.analytics.application.commandservices;

import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.domain.model.commands.CalculatePerformanceIndicatorsCommand;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

public interface PerformanceIndicatorCommandService {

    Result<List<PerformanceIndicator>, ApplicationError> handle(CalculatePerformanceIndicatorsCommand command);
}
