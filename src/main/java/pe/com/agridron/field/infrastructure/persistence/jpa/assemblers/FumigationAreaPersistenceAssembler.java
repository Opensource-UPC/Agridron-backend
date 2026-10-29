package pe.com.agridron.field.infrastructure.persistence.jpa.assemblers;

import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FumigationAreaPersistenceEntity;

/**
 * Static assembler between the fumigation area aggregate and its JPA representation.
 */
public final class FumigationAreaPersistenceAssembler {

    private FumigationAreaPersistenceAssembler() {
    }

    /**
     * Rebuilds the fumigation area aggregate from its persisted state.
     */
    public static FumigationArea toDomainFromPersistence(FumigationAreaPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return FumigationArea.reconstitute(
                entity.getId(),
                entity.getParcelId(),
                FieldGeometry.from(entity.getGeometry()));
    }

    /**
     * Builds a persistence entity from a fumigation area aggregate.
     */
    public static FumigationAreaPersistenceEntity toPersistenceFromDomain(FumigationArea area) {
        if (area == null) {
            return null;
        }
        var entity = new FumigationAreaPersistenceEntity(
                area.getParcelId(),
                area.getGeometry().polygon(),
                area.getGeometry().areaHectares());
        entity.setId(area.getId());
        return entity;
    }

    /**
     * Copies the mutable state of the aggregate into an existing entity, preserving
     * its identity and optimistic-locking version.
     */
    public static void copyToPersistence(FumigationArea area, FumigationAreaPersistenceEntity entity) {
        entity.updateGeometry(area.getGeometry().polygon(), area.getGeometry().areaHectares());
    }
}
