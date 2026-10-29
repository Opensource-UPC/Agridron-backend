package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.repositories.FarmRepository;
import pe.com.agridron.field.infrastructure.persistence.jpa.assemblers.FarmPersistenceAssembler;
import pe.com.agridron.field.infrastructure.persistence.jpa.repositories.FarmPersistenceRepository;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the farm repository port with Spring Data JPA.
 *
 * <p>Acts as the event-publishing boundary: a brand-new farm gets its identity from the
 * database, then {@code onCreated()} registers the creation event. Events registered on
 * the aggregate before saving an existing farm (supervisor changes) are published too.</p>
 */
@Repository
public class FarmRepositoryImpl implements FarmRepository {

    private final FarmPersistenceRepository farmPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public FarmRepositoryImpl(
            FarmPersistenceRepository farmPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.farmPersistenceRepository = farmPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Farm save(Farm farm) {
        var isNew = farm.getId() == null;
        var entity = isNew
                ? FarmPersistenceAssembler.toPersistenceFromDomain(farm)
                : farmPersistenceRepository.findById(farm.getId())
                        .orElseGet(() -> FarmPersistenceAssembler.toPersistenceFromDomain(farm));

        if (!isNew) {
            FarmPersistenceAssembler.copyToPersistence(farm, entity);
        }

        var savedEntity = farmPersistenceRepository.save(entity);
        var savedFarm = FarmPersistenceAssembler.toDomainFromPersistence(savedEntity);

        if (isNew) {
            savedFarm.onCreated();
            domainEventPublisher.publishAndClearEvents(savedFarm);
        }
        domainEventPublisher.publishAndClearEvents(farm);
        return savedFarm;
    }

    @Override
    public Optional<Farm> findById(Long id) {
        return farmPersistenceRepository.findById(id)
                .map(FarmPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Farm> findAllByOwnerId(Long ownerId) {
        return farmPersistenceRepository.findAllByOwnerIdOrderByNameAsc(ownerId).stream()
                .map(FarmPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByOwnerIdAndName(Long ownerId, String name) {
        return farmPersistenceRepository.existsByOwnerIdAndName(ownerId, name);
    }

    @Override
    public boolean existsByOwnerIdAndNameAndIdNot(Long ownerId, String name, Long id) {
        return farmPersistenceRepository.existsByOwnerIdAndNameAndIdNot(ownerId, name, id);
    }
}
