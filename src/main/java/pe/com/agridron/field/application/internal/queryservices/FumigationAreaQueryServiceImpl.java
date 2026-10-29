package pe.com.agridron.field.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.field.application.queryservices.FumigationAreaQueryService;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreaByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreasByParcelQuery;
import pe.com.agridron.field.domain.repositories.FumigationAreaRepository;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Read-only implementation of the fumigation area query service.
 */
@Service
@Transactional(readOnly = true)
public class FumigationAreaQueryServiceImpl implements FumigationAreaQueryService {

    private final FumigationAreaRepository fumigationAreaRepository;
    private final ParcelRepository parcelRepository;

    public FumigationAreaQueryServiceImpl(
            FumigationAreaRepository fumigationAreaRepository,
            ParcelRepository parcelRepository) {
        this.fumigationAreaRepository = fumigationAreaRepository;
        this.parcelRepository = parcelRepository;
    }

    @Override
    public Result<FumigationArea, ApplicationError> handle(GetFumigationAreaByIdQuery query) {
        return fumigationAreaRepository.findById(query.fumigationAreaId())
                .<Result<FumigationArea, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(notFound(
                        "Fumigation Area", query.fumigationAreaId())));
    }

    @Override
    public Result<List<FumigationArea>, ApplicationError> handle(GetFumigationAreasByParcelQuery query) {
        if (parcelRepository.findById(query.parcelId()).isEmpty()) {
            return Result.failure(notFound("Parcel", query.parcelId()));
        }
        return Result.success(fumigationAreaRepository.findByParcelId(query.parcelId()));
    }

    private ApplicationError notFound(String resource, Long id) {
        return ApplicationError.notFound(resource, String.valueOf(id));
    }
}
