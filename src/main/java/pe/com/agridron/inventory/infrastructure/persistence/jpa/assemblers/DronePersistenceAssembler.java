package pe.com.agridron.inventory.infrastructure.persistence.jpa.assemblers;


import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleId;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.entities.DronePersistenceEntity;

/**
 * Convierte entre el agregado {@link Drone} y su fila JPA.
 */
public final class DronePersistenceAssembler {

    private DronePersistenceAssembler() {
    }

    public static Drone toDomainFromPersistence(DronePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return Drone.reconstitute(
                entity.getId(),
                entity.getSerialNumber(),
                entity.getModel(),
                entity.getBatteryLevel(),
                entity.getStatus(),
                new NozzleId(entity.getNozzleId()),
                entity.getTankCapacityLiters(),
                entity.getFlightHours(),
                entity.getLastMaintenanceDate(),
                entity.getNextMaintenanceDate(),
                entity.getUrlimg());
    }

    public static DronePersistenceEntity toPersistenceFromDomain(Drone drone) {
        if (drone == null) {
            return null;
        }
        var entity = new DronePersistenceEntity(
                drone.getSerialNumber(),
                drone.getModel(),
                drone.getBatteryLevel(),
                drone.getStatus(),
                drone.getNozzleId().id(),
                drone.getTankCapacityLiters(),
                drone.getFlightHours(),
                drone.getLastMaintenanceDate(),
                drone.getNextMaintenanceDate(),
                drone.getUrlimg());
        entity.setId(drone.getId());
        return entity;
    }

    /**
     * Copia el estado del agregado sobre una fila que ya existe.
     */
    public static void copyToPersistence(Drone drone, DronePersistenceEntity entity) {
        entity.update(
                drone.getModel(),
                drone.getBatteryLevel(),
                drone.getStatus(),
                drone.getNozzleId().id(),
                drone.getTankCapacityLiters(),
                drone.getFlightHours(),
                drone.getLastMaintenanceDate(),
                drone.getNextMaintenanceDate(),
                drone.getUrlimg());
    }
}