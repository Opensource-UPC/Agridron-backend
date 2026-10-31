package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.interfaces.rest.resources.PerformanceIndicatorResource;

public final class PerformanceIndicatorResourceFromEntityAssembler {

    private PerformanceIndicatorResourceFromEntityAssembler() {
    }

    public static PerformanceIndicatorResource toResourceFromEntity(PerformanceIndicator indicator) {
        return new PerformanceIndicatorResource(
                indicator.getId(),
                indicator.getName(),
                indicator.getValue(),
                indicator.getUnit(),
                indicator.getPeriod(),
                indicator.getCalculatedAt());
    }
}
