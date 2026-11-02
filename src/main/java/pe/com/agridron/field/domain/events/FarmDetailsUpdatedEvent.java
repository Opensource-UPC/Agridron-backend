package pe.com.agridron.field.domain.events;

import pe.com.agridron.field.domain.model.aggregates.Farm;

/**
 * Raised when the descriptive data of a farm changes.
 */
public record FarmDetailsUpdatedEvent(
        Long farmId,
        Long ownerId,
        String name,
        String location,
        String department) {

    public static FarmDetailsUpdatedEvent from(Farm farm) {
        return new FarmDetailsUpdatedEvent(
                farm.getId(),
                farm.getOwnerId(),
                farm.getName(),
                farm.getLocation(),
                farm.getDepartment());
    }
}
