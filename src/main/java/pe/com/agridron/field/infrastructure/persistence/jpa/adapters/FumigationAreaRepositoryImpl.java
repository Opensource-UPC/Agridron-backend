package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.repositories.FumigationAreaRepository;
import pe.com.agridron.field.infrastructure.persistence.jpa.assemblers.FumigationAreaPersistenceAssembler;
import pe.com.agridron.field.infrastructure.persistence.jpa.repositories.FumigationAreaPersistenceRepository;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the fumigation area repository port with Spring Data JPA.
 *
 * <p>Creation events are registered after the database assigns the identity; update
 * events registered by the aggregate before saving are published in the same transaction.</p>
 */
@Repository
public class FumigationAreaRepositoryImpl implements FumigationAreaRepository {

    private final FumigationAreaPersistenceRepository fumigationAreaPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public FumigationAreaRepositoryImpl(
            FumigationAreaPersistenceRepository fumigationAreaPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.fumigationAreaPersistenceRepository = fumigationAreaPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public FumigationArea save(FumigationArea area) {
        var isNew = area.getId() == null;
        var entity = isNew
                ? FumigationAreaPersistenceAssembler.toPersistenceFromDomain(area)
                : fumigationAreaPersistenceRepository.findById(area.getId())
                        .orElseGet(() -> FumigationAreaPersistenceAssembler.toPersistenceFromDomain(area));

        if (!isNew) {
            FumigationAreaPersistenceAssembler.copyToPersistence(area, entity);
        }

        var savedEntity = fumigationAreaPersistenceRepository.save(entity);
        var savedArea = FumigationAreaPersistenceAssembler.toDomainFromPersistence(savedEntity);

        if (isNew) {
            savedArea.onCreated();
            domainEventPublisher.publishAndClearEvents(savedArea);
        }
        domainEventPublisher.publishAndClearEvents(area);
        return savedArea;
    }

    @Override
    public Optional<FumigationArea> findById(Long id) {
        return fumigationAreaPersistenceRepository.findById(id)
                .map(FumigationAreaPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<FumigationArea> findByParcelId(Long parcelId) {
        return fumigationAreaPersistenceRepository.findAllByParcelIdOrderByIdAsc(parcelId).stream()
                .map(FumigationAreaPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return fumigationAreaPersistenceRepository.existsById(id);
    }
}
