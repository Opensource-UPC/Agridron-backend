package pe.com.agridron.field.infrastructure.persistence.jpa.assemblers;

import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.ParcelPersistenceEntity;

/**
 * Static assembler between the parcel aggregate and its JPA representation.
 *
 * <p>The geodesic area is recalculated from the polygon when the aggregate is rebuilt;
 * the persisted {@code area_hectares} column is a denormalized value used for filtering.</p>
 */
public final class ParcelPersistenceAssembler {

    private ParcelPersistenceAssembler() {
    }

    /**
     * Rebuilds the parcel aggregate from its persisted state.
     */
    public static Parcel toDomainFromPersistence(ParcelPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return Parcel.reconstitute(
                entity.getId(),
                entity.getFarmId(),
                entity.getCropId(),
                entity.getName(),
                FieldGeometry.from(entity.getGeometry()),
                entity.getStatus(),
                entity.getPhenologicalStage());
    }

    /**
     * Builds a persistence entity from a parcel aggregate.
     */
    public static ParcelPersistenceEntity toPersistenceFromDomain(Parcel parcel) {
        if (parcel == null) {
            return null;
        }
        var entity = new ParcelPersistenceEntity(
                parcel.getFarmId(),
                parcel.getCropId(),
                parcel.getName(),
                parcel.getGeometry().polygon(),
                parcel.getGeometry().areaHectares(),
                parcel.getStatus(),
                parcel.getPhenologicalStage());
        entity.setId(parcel.getId());
        return entity;
    }

    /**
     * Copies the mutable state of the aggregate into an existing entity, preserving
     * its identity and optimistic-locking version.
     */
    public static void copyToPersistence(Parcel parcel, ParcelPersistenceEntity entity) {
        entity.updateDetails(
                parcel.getName(),
                parcel.getCropId(),
                parcel.getGeometry().polygon(),
                parcel.getGeometry().areaHectares(),
                parcel.getStatus(),
                parcel.getPhenologicalStage());
    }
}
