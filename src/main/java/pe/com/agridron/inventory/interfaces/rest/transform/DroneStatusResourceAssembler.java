package pe.com.agridron.inventory.interfaces.rest.transform;


import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;

import java.util.Locale;

/**
 * Convierte {@link DroneStatus} al formato del frontend y viceversa:
 * {@code AVAILABLE} ↔ {@code available}, {@code IN_FLIGHT} ↔ {@code in-flight}, etc.
 */
public final class DroneStatusResourceAssembler {

    private DroneStatusResourceAssembler() {
    }

    public static String toResourceFromStatus(DroneStatus status) {
        return status.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Acepta {@code in-flight}, {@code IN_FLIGHT} o {@code In-Flight}.
     *
     * @throws IllegalArgumentException si el estado no existe
     */
    public static DroneStatus toStatusFromResource(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        var normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return DroneStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "status must be one of: available, in-flight, maintenance, out-of-service");
        }
    }
}