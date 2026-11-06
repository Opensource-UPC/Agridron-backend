package pe.com.agridron.inventory.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.repositories.DroneRepository;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.assemblers.DronePersistenceAssembler;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.repositories.DronePersistenceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA que implementa el puerto {@link DroneRepository} del dominio.
 */
@Repository
public class DroneRepositoryImpl implements DroneRepository {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("id"));

    private final DronePersistenceRepository dronePersistenceRepository;

    public DroneRepositoryImpl(DronePersistenceRepository dronePersistenceRepository) {
        this.dronePersistenceRepository = dronePersistenceRepository;
    }

    /**
     * Si es nuevo, crea la fila. Si ya existe, carga la fila y le copia el estado del
     * agregado (así se conservan created_at y version, y funciona el bloqueo optimista).
     */
    @Override
    public Drone save(Drone drone) {
        var entity = drone.getId() == null
                ? DronePersistenceAssembler.toPersistenceFromDomain(drone)
                : dronePersistenceRepository.findById(drone.getId())
                .orElseGet(() -> DronePersistenceAssembler.toPersistenceFromDomain(drone));
        if (drone.getId() != null) {
            DronePersistenceAssembler.copyToPersistence(drone, entity);
        }
        var saved = dronePersistenceRepository.save(entity);
        return DronePersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public Optional<Drone> findById(Long id) {
        return dronePersistenceRepository.findById(id)
                .map(DronePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Drone> findAll() {
        return dronePersistenceRepository.findAll(DEFAULT_SORT).stream()
                .map(DronePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return dronePersistenceRepository.existsById(id);
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return dronePersistenceRepository.existsBySerialNumber(serialNumber);
    }
}