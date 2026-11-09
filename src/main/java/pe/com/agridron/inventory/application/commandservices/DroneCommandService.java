package pe.com.agridron.inventory.application.commandservices;


import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.commands.CreateDroneCommand;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Casos de uso que modifican drones.
 */
public interface DroneCommandService {

    /**
     * Registra un dron y descuenta una unidad de su boquilla inicial.
     *
     * @return el dron creado, o un error si la boquilla no existe, no tiene unidades
     *         instalables o el número de serie ya está registrado
     */
    Result<Drone, ApplicationError> handle(CreateDroneCommand command);
}