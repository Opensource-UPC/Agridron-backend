package pe.com.agridron.field.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.field.application.queryservices.CropQueryService;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.model.queries.GetAllCropsQuery;
import pe.com.agridron.field.domain.model.queries.GetCropByIdQuery;
import pe.com.agridron.field.domain.repositories.CropRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Read-only implementation of the crop catalog query service.
 */
@Service
@Transactional(readOnly = true)
public class CropQueryServiceImpl implements CropQueryService {

    private final CropRepository cropRepository;

    public CropQueryServiceImpl(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    @Override
    public Result<Crop, ApplicationError> handle(GetCropByIdQuery query) {
        return cropRepository.findById(query.cropId())
                .<Result<Crop, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("Crop", String.valueOf(query.cropId()))));
    }

    @Override
    public List<Crop> handle(GetAllCropsQuery query) {
        return cropRepository.findAll();
    }
}
