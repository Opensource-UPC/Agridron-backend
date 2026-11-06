package pe.com.agridron.flight.interfaces.rest.transform;

import pe.com.agridron.flight.domain.model.entities.OperationStatus;
import pe.com.agridron.flight.interfaces.rest.resources.OperationStatusResource;

/**
 * Assembler that converts an {@link OperationStatus} entity into an {@link OperationStatusResource}.
 */
public final class OperationStatusResourceFromEntityAssembler {

    private OperationStatusResourceFromEntityAssembler() {
    }

    public static OperationStatusResource toResourceFromEntity(OperationStatus operationStatus) {
        var coordinates = operationStatus.getFlightCoordinates();
        return new OperationStatusResource(
                coordinates.latitude(),
                coordinates.longitude(),
                operationStatus.getStatus(),
                operationStatus.getTimestamp());
    }
}
