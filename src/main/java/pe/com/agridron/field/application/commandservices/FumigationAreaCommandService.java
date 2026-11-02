package pe.com.agridron.field.application.commandservices;

import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.commands.DelimitFumigationAreaCommand;
import pe.com.agridron.field.domain.model.commands.RedefineFumigationAreaCommand;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Application service that handles fumigation area write commands.
 */
public interface FumigationAreaCommandService {

    /**
     * Delimits a new fumigation area inside a parcel.
     *
     * @param command the delimitation command
     * @return the created area, a not-found error or a containment business-rule violation
     */
    Result<FumigationArea, ApplicationError> handle(DelimitFumigationAreaCommand command);

    /**
     * Redefines the boundary of an existing fumigation area.
     *
     * @param command the redefinition command
     * @return the updated area, a not-found error or a containment business-rule violation
     */
    Result<FumigationArea, ApplicationError> handle(RedefineFumigationAreaCommand command);
}
