package pe.com.agridron.inventory.application.commandservices;


import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.commands.CreateNozzleCommand;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Casos de uso que modifican boquillas.
 */
public interface NozzleCommandService {

    /**
     * Registra un nuevo tipo de boquilla con su stock inicial.
     */
    Result<Nozzle, ApplicationError> handle(CreateNozzleCommand command);
}