

package pe.com.agridron.inventory.domain.model.aggregates;


import lombok.Getter;
import pe.com.agridron.inventory.domain.model.services.DomainValidation;
import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleId;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.LocalDate;

/**
 * Dron de la flota (agregado raíz de Inventory & Resource Management).
 *
 * <p>Agregado de dominio sin anotaciones JPA (ver ADR-003). Por ahora solo tiene lo que
 * necesitan los endpoints de registro y consulta; los métodos de cambio de boquilla,
 * incidentes y mantenimiento se agregan con sus endpoints.</p>
 */
@Getter
public class Drone extends AbstractDomainAggregateRoot<Drone> {

    private static final int SERIAL_NUMBER_MAX_LENGTH = 40;
    private static final int MODEL_MAX_LENGTH = 80;
    private static final int URL_IMG_MAX_LENGTH = 500;

    private Long id;
    private final String serialNumber;
    private final String model;
    private final int batteryLevel;
    private DroneStatus status;
    private NozzleId nozzleId;
    private final Double tankCapacityLiters;
    private int flightHours;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private final String urlimg;

    private Drone(String serialNumber, String model, int batteryLevel, DroneStatus status,
                  NozzleId nozzleId, Double tankCapacityLiters, int flightHours,
                  LocalDate lastMaintenanceDate, LocalDate nextMaintenanceDate, String urlimg) {
        this.serialNumber = DomainValidation.requireText(serialNumber, "serialNumber", SERIAL_NUMBER_MAX_LENGTH);
        this.model = DomainValidation.requireText(model, "model", MODEL_MAX_LENGTH);
        this.batteryLevel = DomainValidation.requireBetween(batteryLevel, 0, 100, "batteryLevel");
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        this.status = status;
        if (nozzleId == null) {
            throw new IllegalArgumentException("nozzleId is required");
        }
        this.nozzleId = nozzleId;
        this.tankCapacityLiters = DomainValidation.requirePositive(tankCapacityLiters, "tankCapacityLiters");
        this.flightHours = DomainValidation.requireNonNegative(flightHours, "flightHours");
        requireMaintenanceDatesInOrder(lastMaintenanceDate, nextMaintenanceDate);
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.nextMaintenanceDate = nextMaintenanceDate;
        this.urlimg = DomainValidation.optionalText(urlimg, URL_IMG_MAX_LENGTH, "urlimg");
    }

    /**
     * Registra un dron nuevo en la flota (DR-001): queda disponible y con 0 horas de vuelo.
     *
     * <p>Descontar la unidad de la boquilla es responsabilidad del command service, porque
     * la boquilla es otro agregado.</p>
     */
    public static Drone register(String serialNumber, String model, int batteryLevel,
                                 NozzleId nozzleId, Double tankCapacityLiters,
                                 LocalDate lastMaintenanceDate, LocalDate nextMaintenanceDate,
                                 String urlimg) {
        return new Drone(serialNumber, model, batteryLevel, DroneStatus.AVAILABLE, nozzleId,
                tankCapacityLiters, 0, lastMaintenanceDate, nextMaintenanceDate, urlimg);
    }

    /**
     * Reconstruye un dron existente a partir de lo guardado en base de datos.
     */
    public static Drone reconstitute(Long id, String serialNumber, String model, int batteryLevel,
                                     DroneStatus status, NozzleId nozzleId, Double tankCapacityLiters,
                                     int flightHours, LocalDate lastMaintenanceDate,
                                     LocalDate nextMaintenanceDate, String urlimg) {
        var drone = new Drone(serialNumber, model, batteryLevel, status, nozzleId, tankCapacityLiters,
                flightHours, lastMaintenanceDate, nextMaintenanceDate, urlimg);
        drone.id = DomainValidation.requirePositive(id, "id");
        return drone;
    }

    /** Indica si el dron puede asignarse a una misión. */
    public boolean isAvailable() {
        return status == DroneStatus.AVAILABLE;
    }

    private static void requireMaintenanceDatesInOrder(LocalDate last, LocalDate next) {
        if (last != null && next != null && next.isBefore(last)) {
            throw new IllegalArgumentException(
                    "nextMaintenanceDate must not be before lastMaintenanceDate");
        }
    }
}