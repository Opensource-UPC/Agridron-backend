package pe.com.agridron.inventory.domain.model.commands;


import java.time.LocalDate;

/**
 * Comando para registrar un dron nuevo en la flota (DR-001).
 *
 * <p>No lleva estado ni horas de vuelo: un dron nuevo siempre queda disponible y con 0
 * horas. Las fechas de mantenimiento y la imagen son opcionales.</p>
 *
 * @param serialNumber        número de serie, único en la flota
 * @param model               modelo del dron
 * @param batteryLevel        nivel de batería (0 a 100)
 * @param nozzleId            boquilla inicial que se instala en el dron
 * @param tankCapacityLiters  capacidad del tanque en litros
 * @param lastMaintenanceDate fecha del último mantenimiento (opcional)
 * @param nextMaintenanceDate fecha del próximo mantenimiento (opcional)
 * @param urlimg              URL de la imagen del dron (opcional)
 */
public record CreateDroneCommand(
        String serialNumber,
        String model,
        int batteryLevel,
        Long nozzleId,
        Double tankCapacityLiters,
        LocalDate lastMaintenanceDate,
        LocalDate nextMaintenanceDate,
        String urlimg) {

    public CreateDroneCommand {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("serialNumber is required");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model is required");
        }
        if (nozzleId == null || nozzleId <= 0) {
            throw new IllegalArgumentException("nozzleId must be positive");
        }
        if (tankCapacityLiters == null || tankCapacityLiters <= 0) {
            throw new IllegalArgumentException("tankCapacityLiters must be positive");
        }
    }
}