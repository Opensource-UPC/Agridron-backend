package pe.com.agridron.inventory.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.inventory.application.commandservices.DroneCommandService;
import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.commands.CreateDroneCommand;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleId;
import pe.com.agridron.inventory.domain.repositories.DroneRepository;
import pe.com.agridron.inventory.domain.repositories.NozzleRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Implementación de los casos de uso que modifican drones.
 *
 * <p>Aquí se revisan las reglas de negocio que involucran a más de un agregado o al
 * repositorio, antes de tocar el dominio. Si una regla falla se devuelve
 * {@code Result.failure(...)} y no se guarda nada.</p>
 */
@Service
public class DroneCommandServiceImpl implements DroneCommandService {

    private static final String DRONE = "Drone";
    private static final String NOZZLE = "Nozzle";
    private static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";


    private final DroneRepository droneRepository;
    private final NozzleRepository nozzleRepository;

    public DroneCommandServiceImpl(DroneRepository droneRepository, NozzleRepository nozzleRepository) {
        this.droneRepository = droneRepository;
        this.nozzleRepository = nozzleRepository;
    }



    /**
     * Registra un dron (DR-001) e instala su boquilla inicial.
     *
     * <ol>
     *   <li>Los datos del dron deben ser válidos (400 VALIDATION_ERROR).</li>
     *   <li>El número de serie no puede repetirse (409 DRONE_CONFLICT).</li>
     *   <li>La boquilla debe existir (404 NOZZLE_NOT_FOUND).</li>
     *   <li>La boquilla debe tener una unidad buena o desgastada (422 INSUFFICIENT_STOCK).</li>
     *   <li>Se descuenta la unidad y se guarda el dron en la misma transacción.</li>
     * </ol>
     */
    @Override
    @Transactional
    public Result<Drone, ApplicationError> handle(CreateDroneCommand command) {
        Drone drone;
        try {
            drone = Drone.register(
                    command.serialNumber(),
                    command.model(),
                    command.batteryLevel(),
                    new NozzleId(command.nozzleId()),
                    command.tankCapacityLiters(),
                    command.lastMaintenanceDate(),
                    command.nextMaintenanceDate(),
                    command.urlimg());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("drone", ex.getMessage()));
        }

        if (droneRepository.existsBySerialNumber(drone.getSerialNumber())) {
            return Result.failure(ApplicationError.conflict(
                    DRONE,
                    "A drone with serial number '%s' already exists".formatted(drone.getSerialNumber())));
        }

        var nozzle = nozzleRepository.findById(command.nozzleId()).orElse(null);
        if (nozzle == null) {
            return Result.failure(ApplicationError.notFound(NOZZLE, String.valueOf(command.nozzleId())));
        }

        if (!nozzle.hasInstallableUnits()) {
            return Result.failure(new ApplicationError(
                    INSUFFICIENT_STOCK,
                    "Insufficient stock for %s".formatted(NOZZLE),
                    "Nozzle %d has no good or worn units to install".formatted(nozzle.getId())));
        }

        nozzle.takeOneUnit();
        nozzleRepository.save(nozzle);
        return Result.success(droneRepository.save(drone));
    }
}