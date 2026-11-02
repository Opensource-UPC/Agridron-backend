package pe.com.agridron.field.domain.events;

import pe.com.agridron.field.domain.model.aggregates.Farm;

/**
 * Raised when a farm has been persisted and its identity is available.
 */
public record FarmRegisteredEvent(
        Long farmId,
        Long ownerId,
        String name,
        String location,
        String department) {

    public static FarmRegisteredEvent from(Farm farm) {
        return new FarmRegisteredEvent(
                farm.getId(),
                farm.getOwnerId(),
                farm.getName(),
                farm.getLocation(),
                farm.getDepartment());
    }
}
