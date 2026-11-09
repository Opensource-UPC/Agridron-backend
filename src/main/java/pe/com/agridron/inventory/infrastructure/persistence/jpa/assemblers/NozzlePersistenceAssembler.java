package pe.com.agridron.inventory.infrastructure.persistence.jpa.assemblers;


import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleStock;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.entities.NozzlePersistenceEntity;

/**
 * Convierte entre el agregado {@link Nozzle} y su fila JPA.
 */
public final class NozzlePersistenceAssembler {

    private NozzlePersistenceAssembler() {
    }

    public static Nozzle toDomainFromPersistence(NozzlePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return Nozzle.reconstitute(
                entity.getId(),
                entity.getType(),
                entity.getDiameterMm(),
                new NozzleStock(entity.getGoodUnits(), entity.getWornUnits(), entity.getDamagedUnits()));
    }

    public static NozzlePersistenceEntity toPersistenceFromDomain(Nozzle nozzle) {
        if (nozzle == null) {
            return null;
        }
        var stock = nozzle.getStock();
        var entity = new NozzlePersistenceEntity(
                nozzle.getType(),
                nozzle.getDiameterMm(),
                stock.goodUnits(),
                stock.wornUnits(),
                stock.damagedUnits());
        entity.setId(nozzle.getId());
        return entity;
    }

    /**
     * Copia lo que puede cambiar (el stock) sobre una fila que ya existe.
     */
    public static void copyToPersistence(Nozzle nozzle, NozzlePersistenceEntity entity) {
        var stock = nozzle.getStock();
        entity.updateStock(stock.goodUnits(), stock.wornUnits(), stock.damagedUnits());
    }
}