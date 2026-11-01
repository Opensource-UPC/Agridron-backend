package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortDirection;
import pe.com.agridron.field.domain.model.queries.SortOrder;
import pe.com.agridron.field.domain.repositories.FarmRepository;
import pe.com.agridron.field.infrastructure.persistence.jpa.assemblers.FarmPersistenceAssembler;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.repositories.FarmPersistenceRepository;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

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

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("name"),
            Sort.Order.asc("id"));

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
    public PageResult<Farm> findAllByOwnerId(
            Long ownerId,
            Pagination pagination,
            SortOrder sortOrder) {
        var sort = toSort(sortOrder);
        if (!pagination.paged()) {
            var content = farmPersistenceRepository.findAllByOwnerId(ownerId, sort).stream()
                    .map(FarmPersistenceAssembler::toDomainFromPersistence)
                    .toList();
            return PageResult.unpaged(content);
        }

        var page = farmPersistenceRepository.findAllByOwnerId(
                ownerId,
                PageRequest.of(pagination.page(), pagination.size(), sort));
        return toPageResult(page);
    }

    private static Sort toSort(SortOrder sortOrder) {
        if (sortOrder == null) {
            return DEFAULT_SORT;
        }
        var direction = sortOrder.direction() == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        var primary = Sort.by(new Sort.Order(direction, sortOrder.field()));
        return "id".equals(sortOrder.field())
                ? primary
                : primary.and(Sort.by(Sort.Order.asc("id")));
    }

    private static PageResult<Farm> toPageResult(Page<FarmPersistenceEntity> page) {
        return new PageResult<>(
                page.getContent().stream()
                        .map(FarmPersistenceAssembler::toDomainFromPersistence)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
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
