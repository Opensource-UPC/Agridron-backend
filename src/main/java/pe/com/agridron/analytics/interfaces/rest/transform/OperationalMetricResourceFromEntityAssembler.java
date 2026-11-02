package pe.com.agridron.analytics.interfaces.rest.transform;

import pe.com.agridron.analytics.domain.model.entities.OperationalMetric;
import pe.com.agridron.analytics.interfaces.rest.resources.OperationalMetricResource;

public final class OperationalMetricResourceFromEntityAssembler {

    private OperationalMetricResourceFromEntityAssembler() {
    }

    public static OperationalMetricResource toResourceFromEntity(OperationalMetric metric) {
        return new OperationalMetricResource(
                metric.getId(), metric.getName(), metric.getValue(), metric.getUnit());
    }
}
