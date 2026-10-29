package pe.com.agridron.field.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.field.application.queryservices.ParcelQueryService;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.queries.GetParcelByIdQuery;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Read-only implementation of the parcel query service.
 */
@Service
@Transactional(readOnly = true)
public class ParcelQueryServiceImpl implements ParcelQueryService {

    private final ParcelRepository parcelRepository;

    public ParcelQueryServiceImpl(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    @Override
    public Result<Parcel, ApplicationError> handle(GetParcelByIdQuery query) {
        return parcelRepository.findById(query.parcelId())
                .<Result<Parcel, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("Parcel", String.valueOf(query.parcelId()))));
    }

    @Override
    public List<Parcel> handle(ParcelSearchCriteria criteria) {
        return parcelRepository.search(criteria);
    }
}
