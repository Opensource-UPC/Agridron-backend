package pe.com.agridron.field.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.field.application.queryservices.FarmQueryService;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.queries.GetFarmByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFarmsByOwnerQuery;
import pe.com.agridron.field.domain.repositories.FarmRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Read-only implementation of the farm query service.
 */
@Service
@Transactional(readOnly = true)
public class FarmQueryServiceImpl implements FarmQueryService {

    private final FarmRepository farmRepository;

    public FarmQueryServiceImpl(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    @Override
    public Result<Farm, ApplicationError> handle(GetFarmByIdQuery query) {
        return farmRepository.findById(query.farmId())
                .<Result<Farm, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("Farm", String.valueOf(query.farmId()))));
    }

    @Override
    public List<Farm> handle(GetFarmsByOwnerQuery query) {
        return farmRepository.findAllByOwnerId(query.ownerId());
    }
}
