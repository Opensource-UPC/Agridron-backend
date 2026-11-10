package pe.com.agridron.inventory.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.inventory.application.commandservices.NozzleCommandService;
import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.commands.CreateNozzleCommand;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleStock;
import pe.com.agridron.inventory.domain.repositories.NozzleRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Implementación de los casos de uso que modifican boquillas.
 */
@Service
public class NozzleCommandServiceImpl implements NozzleCommandService {

    private final NozzleRepository nozzleRepository;

    public NozzleCommandServiceImpl(NozzleRepository nozzleRepository) {
        this.nozzleRepository = nozzleRepository;
    }

    /**
     * Registra un tipo de boquilla con su stock inicial (DR-004).
     * Datos inválidos → 400 VALIDATION_ERROR.
     */
    @Override
    @Transactional
    public Result<Nozzle, ApplicationError> handle(CreateNozzleCommand command) {
        Nozzle nozzle;
        try {
            var stock = new NozzleStock(command.goodUnits(), command.wornUnits(), command.damagedUnits());
            nozzle = Nozzle.register(command.type(), command.diameterMm(), stock);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("nozzle", ex.getMessage()));
        }
        return Result.success(nozzleRepository.save(nozzle));
    }
}