package pe.com.agridron.field.infrastructure.persistence.jpa.assemblers;

import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;

/**
 * Static assembler between the farm aggregate and its JPA representation.
 */
public final class FarmPersistenceAssembler {

    private FarmPersistenceAssembler() {
    }

    /**
     * Rebuilds the farm aggregate from its persisted state.
     */
    public static Farm toDomainFromPersistence(FarmPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return Farm.reconstitute(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getLocation(),
                entity.getDepartment(),
                entity.getSupervisorIds());
    }

    /**
     * Builds a persistence entity from a farm aggregate.
     */
    public static FarmPersistenceEntity toPersistenceFromDomain(Farm farm) {
        if (farm == null) {
            return null;
        }
        var entity = new FarmPersistenceEntity(
                farm.getOwnerId(),
                farm.getName(),
                farm.getLocation(),
                farm.getDepartment());
        entity.setId(farm.getId());
        entity.replaceSupervisors(farm.getSupervisorIds());
        return entity;
    }

    /**
     * Copies the mutable state of the aggregate into an existing entity, preserving
     * its identity and optimistic-locking version.
     */
    public static void copyToPersistence(Farm farm, FarmPersistenceEntity entity) {
        entity.updateDetails(farm.getName(), farm.getLocation(), farm.getDepartment());
        entity.replaceSupervisors(farm.getSupervisorIds());
    }
}
