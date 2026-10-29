package pe.com.agridron.field.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.field.application.commandservices.FarmCommandService;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.commands.AssignFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.CreateFarmCommand;
import pe.com.agridron.field.domain.model.commands.RemoveFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.UpdateFarmCommand;
import pe.com.agridron.field.domain.repositories.FarmRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Transactional implementation of the farm command service.
 */
@Service
@Transactional
public class FarmCommandServiceImpl implements FarmCommandService {

    private final FarmRepository farmRepository;

    public FarmCommandServiceImpl(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    @Override
    public Result<Farm, ApplicationError> handle(CreateFarmCommand command) {
        var farm = Farm.register(
                command.ownerId(),
                command.name(),
                command.location(),
                command.department());

        if (farmRepository.existsByOwnerIdAndName(farm.getOwnerId(), farm.getName())) {
            return Result.failure(ApplicationError.conflict(
                    "Farm",
                    "A farm named '%s' already exists for this owner".formatted(farm.getName())));
        }

        return Result.success(farmRepository.save(farm));
    }

    @Override
    public Result<Farm, ApplicationError> handle(UpdateFarmCommand command) {
        var farm = farmRepository.findById(command.farmId()).orElse(null);
        if (farm == null) {
            return Result.failure(notFound(command.farmId()));
        }

        farm.updateDetails(command.name(), command.location(), command.department());

        if (farmRepository.existsByOwnerIdAndNameAndIdNot(
                farm.getOwnerId(), farm.getName(), farm.getId())) {
            return Result.failure(ApplicationError.conflict(
                    "Farm",
                    "A farm named '%s' already exists for this owner".formatted(farm.getName())));
        }

        return Result.success(farmRepository.save(farm));
    }

    @Override
    public Result<Farm, ApplicationError> handle(AssignFarmSupervisorCommand command) {
        var farm = farmRepository.findById(command.farmId()).orElse(null);
        if (farm == null) {
            return Result.failure(notFound(command.farmId()));
        }

        farm.assignSupervisor(command.supervisorId());
        return Result.success(farmRepository.save(farm));
    }

    @Override
    public Result<Farm, ApplicationError> handle(RemoveFarmSupervisorCommand command) {
        var farm = farmRepository.findById(command.farmId()).orElse(null);
        if (farm == null) {
            return Result.failure(notFound(command.farmId()));
        }

        farm.removeSupervisor(command.supervisorId());
        return Result.success(farmRepository.save(farm));
    }

    private ApplicationError notFound(Long farmId) {
        return ApplicationError.notFound("Farm", String.valueOf(farmId));
    }
}
