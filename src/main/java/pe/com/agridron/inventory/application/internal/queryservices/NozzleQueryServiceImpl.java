package pe.com.agridron.inventory.application.internal.queryservices;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.inventory.application.queryservices.NozzleQueryService;
import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.queries.GetAllNozzlesQuery;
import pe.com.agridron.inventory.domain.model.queries.GetNozzleByIdQuery;
import pe.com.agridron.inventory.domain.repositories.NozzleRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Implementación de las consultas de boquillas.
 */
@Service
@Transactional(readOnly = true)
public class NozzleQueryServiceImpl implements NozzleQueryService {

    private final NozzleRepository nozzleRepository;

    public NozzleQueryServiceImpl(NozzleRepository nozzleRepository) {
        this.nozzleRepository = nozzleRepository;
    }

    @Override
    public Result<Nozzle, ApplicationError> handle(GetNozzleByIdQuery query) {
        return nozzleRepository.findById(query.nozzleId())
                .<Result<Nozzle, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("Nozzle", String.valueOf(query.nozzleId()))));
    }

    @Override
    public List<Nozzle> handle(GetAllNozzlesQuery query) {
        return nozzleRepository.findAll();
    }
}