package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.commands.AddOperationalMetricCommand;
import pe.com.agridron.analytics.interfaces.rest.resources.AddOperationalMetricResource;

public final class AddOperationalMetricCommandFromResourceAssembler {

    private AddOperationalMetricCommandFromResourceAssembler() {
    }

    public static AddOperationalMetricCommand toCommandFromResource(
            Long reportId, AddOperationalMetricResource resource) {
        return new AddOperationalMetricCommand(reportId, resource.name(), resource.value(), resource.unit());
    }
}
