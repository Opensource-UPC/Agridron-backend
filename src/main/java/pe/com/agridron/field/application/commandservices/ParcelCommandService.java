package pe.com.agridron.field.application.commandservices;

import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.commands.ChangeParcelStatusCommand;
import pe.com.agridron.field.domain.model.commands.CreateParcelCommand;
import pe.com.agridron.field.domain.model.commands.RedefineParcelGeometryCommand;
import pe.com.agridron.field.domain.model.commands.UpdateParcelCommand;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Application service that handles parcel write commands.
 */
public interface ParcelCommandService {

    /**
     * Creates a parcel inside an existing farm.
     *
     * @param command the creation command
     * @return the created parcel or a not-found/conflict error
     */
    Result<Parcel, ApplicationError> handle(CreateParcelCommand command);

    /**
     * Updates the name and crop information of a parcel.
     *
     * @param command the update command
     * @return the updated parcel or a not-found/conflict error
     */
    Result<Parcel, ApplicationError> handle(UpdateParcelCommand command);

    /**
     * Redefines the boundary of a parcel.
     *
     * @param command the geometry command
     * @return the updated parcel or a not-found error
     */
    Result<Parcel, ApplicationError> handle(RedefineParcelGeometryCommand command);

    /**
     * Activates or deactivates a parcel.
     *
     * @param command the status command
     * @return the updated parcel or a not-found error
     */
    Result<Parcel, ApplicationError> handle(ChangeParcelStatusCommand command);
}
