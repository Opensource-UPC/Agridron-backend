package pe.com.agridron.field.infrastructure.persistence.jpa.assemblers;

import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.CropPersistenceEntity;

/**
 * Static assembler between the read-only crop catalog and its JPA representation.
 */
public final class CropPersistenceAssembler {

    private CropPersistenceAssembler() {
    }

    /**
     * Rebuilds the domain crop from its persisted state.
     *
     * @param entity the persistence entity, may be {@code null}
     * @return the domain crop, or {@code null} when the entity is {@code null}
     */
    public static Crop toDomainFromPersistence(CropPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return Crop.reconstitute(entity.getId(), entity.getName(), entity.getVariety());
    }

    /**
     * Builds the persistence entity from a domain crop.
     *
     * @param crop the domain crop, may be {@code null}
     * @return the persistence entity, or {@code null} when the crop is {@code null}
     */
    public static CropPersistenceEntity toPersistenceFromDomain(Crop crop) {
        if (crop == null) {
            return null;
        }
        var entity = new CropPersistenceEntity(crop.getName(), crop.getVariety());
        entity.setId(crop.getId());
        return entity;
    }
}
