package pe.com.agridron.field.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.field.application.commandservices.ParcelCommandService;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.commands.ChangeParcelStatusCommand;
import pe.com.agridron.field.domain.model.commands.CreateParcelCommand;
import pe.com.agridron.field.domain.model.commands.RedefineParcelGeometryCommand;
import pe.com.agridron.field.domain.model.commands.UpdateParcelCommand;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.ports.ParcelUsagePort;
import pe.com.agridron.field.domain.repositories.CropRepository;
import pe.com.agridron.field.domain.repositories.FarmRepository;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Transactional implementation of the parcel command service.
 */
@Service
@Transactional
public class ParcelCommandServiceImpl implements ParcelCommandService {

    private final ParcelRepository parcelRepository;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final ParcelUsagePort parcelUsagePort;

    public ParcelCommandServiceImpl(
            ParcelRepository parcelRepository,
            FarmRepository farmRepository,
            CropRepository cropRepository,
            ParcelUsagePort parcelUsagePort) {
        this.parcelRepository = parcelRepository;
        this.farmRepository = farmRepository;
        this.cropRepository = cropRepository;
        this.parcelUsagePort = parcelUsagePort;
    }

    @Override
    public Result<Parcel, ApplicationError> handle(CreateParcelCommand command) {
        if (farmRepository.findById(command.farmId()).isEmpty()) {
            return Result.failure(notFound("Farm", command.farmId()));
        }
        if (command.cropId() != null && !cropRepository.existsById(command.cropId())) {
            return Result.failure(notFound("Crop", command.cropId()));
        }

        var parcel = Parcel.create(
                command.farmId(),
                command.name(),
                command.geometry(),
                command.cropId(),
                command.phenologicalStage());

        if (parcelRepository.existsByFarmIdAndName(parcel.getFarmId(), parcel.getName())) {
            return Result.failure(ApplicationError.conflict(
                    "Parcel",
                    "A parcel named '%s' already exists in this farm".formatted(parcel.getName())));
        }

        return Result.success(parcelRepository.save(parcel));
    }

    @Override
    public Result<Parcel, ApplicationError> handle(UpdateParcelCommand command) {
        var parcel = parcelRepository.findById(command.parcelId()).orElse(null);
        if (parcel == null) {
            return Result.failure(notFound("Parcel", command.parcelId()));
        }
        if (command.cropId() != null && !cropRepository.existsById(command.cropId())) {
            return Result.failure(notFound("Crop", command.cropId()));
        }

        parcel.updateDetails(command.name(), command.cropId(), command.phenologicalStage());

        if (parcelRepository.existsByFarmIdAndNameAndIdNot(
                parcel.getFarmId(), parcel.getName(), parcel.getId())) {
            return Result.failure(ApplicationError.conflict(
                    "Parcel",
                    "A parcel named '%s' already exists in this farm".formatted(parcel.getName())));
        }

        return Result.success(parcelRepository.save(parcel));
    }

    @Override
    public Result<Parcel, ApplicationError> handle(RedefineParcelGeometryCommand command) {
        var parcel = parcelRepository.findById(command.parcelId()).orElse(null);
        if (parcel == null) {
            return Result.failure(notFound("Parcel", command.parcelId()));
        }

        parcel.redefineGeometry(command.geometry());
        return Result.success(parcelRepository.save(parcel));
    }

    @Override
    public Result<Parcel, ApplicationError> handle(ChangeParcelStatusCommand command) {
        if (command.status() == null) {
            return Result.failure(ApplicationError.validationError("status", "status is required"));
        }

        var parcel = parcelRepository.findById(command.parcelId()).orElse(null);
        if (parcel == null) {
            return Result.failure(notFound("Parcel", command.parcelId()));
        }
        if (command.status() == ParcelStatus.INACTIVE
                && parcelUsagePort.hasActiveMissions(parcel.getId())) {
            return Result.failure(ApplicationError.invalidState(
                    "Parcel",
                    "Parcel %d has active missions and cannot be deactivated".formatted(parcel.getId())));
        }

        switch (command.status()) {
            case ACTIVE -> parcel.activate();
            case INACTIVE -> parcel.deactivate();
        }
        return Result.success(parcelRepository.save(parcel));
    }

    private ApplicationError notFound(String resource, Long id) {
        return ApplicationError.notFound(resource, String.valueOf(id));
    }
}
