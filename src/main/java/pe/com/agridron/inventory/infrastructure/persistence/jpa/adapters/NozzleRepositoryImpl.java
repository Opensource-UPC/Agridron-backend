package pe.com.agridron.inventory.infrastructure.persistence.jpa.adapters;


import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.repositories.NozzleRepository;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.assemblers.NozzlePersistenceAssembler;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.repositories.NozzlePersistenceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA que implementa el puerto {@link NozzleRepository} del dominio.
 */
@Repository
public class NozzleRepositoryImpl implements NozzleRepository {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("id"));

    private final NozzlePersistenceRepository nozzlePersistenceRepository;

    public NozzleRepositoryImpl(NozzlePersistenceRepository nozzlePersistenceRepository) {
        this.nozzlePersistenceRepository = nozzlePersistenceRepository;
    }

    /**
     * Si es nuevo, crea la fila. Si ya existe, carga la fila y le copia el estado del
     * agregado (así se conservan created_at y version, y funciona el bloqueo optimista).
     */
    @Override
    public Nozzle save(Nozzle nozzle) {
        var entity = nozzle.getId() == null
                ? NozzlePersistenceAssembler.toPersistenceFromDomain(nozzle)
                : nozzlePersistenceRepository.findById(nozzle.getId())
                .orElseGet(() -> NozzlePersistenceAssembler.toPersistenceFromDomain(nozzle));
        if (nozzle.getId() != null) {
            NozzlePersistenceAssembler.copyToPersistence(nozzle, entity);
        }
        var saved = nozzlePersistenceRepository.save(entity);
        return NozzlePersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public Optional<Nozzle> findById(Long id) {
        return nozzlePersistenceRepository.findById(id)
                .map(NozzlePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Nozzle> findAll() {
        return nozzlePersistenceRepository.findAll(DEFAULT_SORT).stream()
                .map(NozzlePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return nozzlePersistenceRepository.existsById(id);
    }
}