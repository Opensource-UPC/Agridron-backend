package pe.com.agridron.field.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.field.application.commandservices.FumigationAreaCommandService;
import pe.com.agridron.field.domain.exceptions.FumigationAreaOutsideParcelException;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.commands.DelimitFumigationAreaCommand;
import pe.com.agridron.field.domain.model.commands.RedefineFumigationAreaCommand;
import pe.com.agridron.field.domain.repositories.FumigationAreaRepository;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Transactional implementation of the fumigation area command service.
 *
 * <p>Containment is a domain invariant; the domain exception is translated into a
 * business-rule violation so the REST layer can answer with 422.</p>
 */
@Service
@Transactional
public class FumigationAreaCommandServiceImpl implements FumigationAreaCommandService {

    private final FumigationAreaRepository fumigationAreaRepository;
    private final ParcelRepository parcelRepository;

    public FumigationAreaCommandServiceImpl(
            FumigationAreaRepository fumigationAreaRepository,
            ParcelRepository parcelRepository) {
        this.fumigationAreaRepository = fumigationAreaRepository;
        this.parcelRepository = parcelRepository;
    }

    @Override
    public Result<FumigationArea, ApplicationError> handle(DelimitFumigationAreaCommand command) {
        var parcel = parcelRepository.findById(command.parcelId()).orElse(null);
        if (parcel == null) {
            return Result.failure(notFound("Parcel", command.parcelId()));
        }

        try {
            var area = FumigationArea.delimit(
                    parcel.getId(), parcel.getGeometry(), command.geometry());
            return Result.success(fumigationAreaRepository.save(area));
        } catch (FumigationAreaOutsideParcelException ex) {
            return Result.failure(containmentViolation(ex));
        }
    }

    @Override
    public Result<FumigationArea, ApplicationError> handle(RedefineFumigationAreaCommand command) {
        var area = fumigationAreaRepository.findById(command.fumigationAreaId()).orElse(null);
        if (area == null) {
            return Result.failure(notFound("Fumigation Area", command.fumigationAreaId()));
        }

        var parcel = parcelRepository.findById(area.getParcelId()).orElse(null);
        if (parcel == null) {
            return Result.failure(notFound("Parcel", area.getParcelId()));
        }

        try {
            area.redefine(parcel.getGeometry(), command.geometry());
            return Result.success(fumigationAreaRepository.save(area));
        } catch (FumigationAreaOutsideParcelException ex) {
            return Result.failure(containmentViolation(ex));
        }
    }

    private ApplicationError containmentViolation(FumigationAreaOutsideParcelException ex) {
        return ApplicationError.businessRuleViolation("fumigation-area-containment", ex.getMessage());
    }

    private ApplicationError notFound(String resource, Long id) {
        return ApplicationError.notFound(resource, String.valueOf(id));
    }
}
