package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
import pe.com.agridron.field.infrastructure.persistence.jpa.assemblers.ParcelPersistenceAssembler;
import pe.com.agridron.field.infrastructure.persistence.jpa.repositories.ParcelPersistenceRepository;
import pe.com.agridron.field.infrastructure.persistence.jpa.specifications.ParcelSpecifications;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the parcel repository port with Spring Data JPA.
 *
 * <p>Creation events are registered after the database assigns the identity; update
 * events registered by the aggregate before saving are published in the same transaction.</p>
 */
@Repository
public class ParcelRepositoryImpl implements ParcelRepository {

    private static final Sort SEARCH_ORDER = Sort.by(
            Sort.Order.asc("name"),
            Sort.Order.asc("id"));

    private final ParcelPersistenceRepository parcelPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ParcelRepositoryImpl(
            ParcelPersistenceRepository parcelPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.parcelPersistenceRepository = parcelPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Parcel save(Parcel parcel) {
        var isNew = parcel.getId() == null;
        var entity = isNew
                ? ParcelPersistenceAssembler.toPersistenceFromDomain(parcel)
                : parcelPersistenceRepository.findById(parcel.getId())
                        .orElseGet(() -> ParcelPersistenceAssembler.toPersistenceFromDomain(parcel));

        if (!isNew) {
            ParcelPersistenceAssembler.copyToPersistence(parcel, entity);
        }

        var savedEntity = parcelPersistenceRepository.save(entity);
        var savedParcel = ParcelPersistenceAssembler.toDomainFromPersistence(savedEntity);

        if (isNew) {
            savedParcel.onCreated();
            domainEventPublisher.publishAndClearEvents(savedParcel);
        }
        domainEventPublisher.publishAndClearEvents(parcel);
        return savedParcel;
    }

    @Override
    public Optional<Parcel> findById(Long id) {
        return parcelPersistenceRepository.findById(id)
                .map(ParcelPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Parcel> findByFarmId(Long farmId) {
        return parcelPersistenceRepository.findAllByFarmIdOrderByNameAsc(farmId).stream()
                .map(ParcelPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Parcel> search(ParcelSearchCriteria criteria) {
        return parcelPersistenceRepository
                .findAll(ParcelSpecifications.withCriteria(criteria), SEARCH_ORDER)
                .stream()
                .map(ParcelPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByFarmIdAndName(Long farmId, String name) {
        return parcelPersistenceRepository.existsByFarmIdAndName(farmId, name);
    }

    @Override
    public boolean existsByFarmIdAndNameAndIdNot(Long farmId, String name, Long id) {
        return parcelPersistenceRepository.existsByFarmIdAndNameAndIdNot(farmId, name, id);
    }
}
