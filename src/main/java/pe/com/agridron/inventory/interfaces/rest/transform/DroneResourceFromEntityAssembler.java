package pe.com.agridron.inventory.interfaces.rest.transform;

import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.interfaces.rest.resources.DroneResource;

import java.time.LocalDate;

/**
 * Convierte el agregado {@link Drone} en el JSON de salida.
 */
public final class DroneResourceFromEntityAssembler {

    private DroneResourceFromEntityAssembler() {
    }

    public static DroneResource toResourceFromEntity(Drone drone) {
        return new DroneResource(
                drone.getId(),
                drone.getSerialNumber(),
                drone.getModel(),
                drone.getBatteryLevel(),
                DroneStatusResourceAssembler.toResourceFromStatus(drone.getStatus()),
                drone.getNozzleId().id(),
                drone.getTankCapacityLiters(),
                toIsoDate(drone.getLastMaintenanceDate()),
                toIsoDate(drone.getNextMaintenanceDate()),
                drone.getUrlimg(),
                drone.getFlightHours());
    }

    private static String toIsoDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }
}