package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.commands.CalculatePerformanceIndicatorsCommand;
import pe.com.agridron.analytics.interfaces.rest.resources.CalculatePerformanceIndicatorsResource;

import java.time.Instant;

public final class CalculatePerformanceIndicatorsCommandFromResourceAssembler {

    private CalculatePerformanceIndicatorsCommandFromResourceAssembler() {
    }

    public static CalculatePerformanceIndicatorsCommand toCommandFromResource(
            CalculatePerformanceIndicatorsResource resource, Instant calculatedAt) {
        return new CalculatePerformanceIndicatorsCommand(resource.period(), calculatedAt);
    }
}
