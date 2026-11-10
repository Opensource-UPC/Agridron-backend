package pe.com.agridron.inventory.interfaces.rest.transform;

import pe.com.agridron.inventory.domain.model.commands.CreateDroneCommand;
import pe.com.agridron.inventory.interfaces.rest.resources.CreateDroneResource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Convierte el JSON de entrada en un {@link CreateDroneCommand}.
 */
public final class CreateDroneCommandFromResourceAssembler {

    private CreateDroneCommandFromResourceAssembler() {
    }

    public static CreateDroneCommand toCommandFromResource(CreateDroneResource resource) {
        return new CreateDroneCommand(
                resource.serialNumber(),
                resource.model(),
                resource.batteryLevel(),
                resource.nozzleId(),
                resource.tankCapacityLiters(),
                toOptionalDate(resource.lastMaintenanceDate(), "lastMaintenanceDate"),
                toOptionalDate(resource.nextMaintenanceDate(), "nextMaintenanceDate"),
                resource.urlimg());
    }

    /**
     * Texto vacío o {@code null} → sin fecha. Cualquier otro valor debe ser {@code yyyy-MM-dd}.
     */
    static LocalDate toOptionalDate(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("%s must use the format yyyy-MM-dd".formatted(field));
        }
    }
}
