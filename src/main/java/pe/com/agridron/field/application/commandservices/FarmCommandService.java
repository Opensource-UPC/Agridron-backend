package pe.com.agridron.field.application.commandservices;

import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.commands.AssignFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.CreateFarmCommand;
import pe.com.agridron.field.domain.model.commands.RemoveFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.UpdateFarmCommand;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Application service that handles farm write commands.
 */
public interface FarmCommandService {

    /**
     * Registers a new farm for an owner.
     *
     * @param command the creation command
     * @return the created farm or a {@code FARM_CONFLICT} error when the name is taken
     */
    Result<Farm, ApplicationError> handle(CreateFarmCommand command);

    /**
     * Updates the descriptive data of an existing farm.
     *
     * @param command the update command
     * @return the updated farm or a not-found/conflict error
     */
    Result<Farm, ApplicationError> handle(UpdateFarmCommand command);

    /**
     * Assigns a supervisor to a farm.
     *
     * @param command the assignment command
     * @return the updated farm or a not-found error
     */
    Result<Farm, ApplicationError> handle(AssignFarmSupervisorCommand command);

    /**
     * Removes a supervisor from a farm.
     *
     * @param command the removal command
     * @return the updated farm or a not-found error
     */
    Result<Farm, ApplicationError> handle(RemoveFarmSupervisorCommand command);
}
