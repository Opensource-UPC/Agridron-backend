package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortDirection;
import pe.com.agridron.field.domain.model.queries.SortOrder;
import pe.com.agridron.field.domain.repositories.FumigationAreaRepository;
import pe.com.agridron.field.infrastructure.persistence.jpa.assemblers.FumigationAreaPersistenceAssembler;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FumigationAreaPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.repositories.FumigationAreaPersistenceRepository;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.Optional;

/**
 * Repository adapter that bridges the fumigation area repository port with Spring Data JPA.
 *
 * <p>Creation events are registered after the database assigns the identity; update
 * events registered by the aggregate before saving are published in the same transaction.</p>
 */
@Repository
public class FumigationAreaRepositoryImpl implements FumigationAreaRepository {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("id"));

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
    public PageResult<FumigationArea> findByParcelId(
            Long parcelId,
            Pagination pagination,
            SortOrder sortOrder) {
        var sort = toSort(sortOrder);
        if (!pagination.paged()) {
            var content = fumigationAreaPersistenceRepository
                    .findAllByParcelId(parcelId, sort).stream()
                    .map(FumigationAreaPersistenceAssembler::toDomainFromPersistence)
                    .toList();
            return PageResult.unpaged(content);
        }

        var page = fumigationAreaPersistenceRepository.findAllByParcelId(
                parcelId,
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

    private static PageResult<FumigationArea> toPageResult(
            Page<FumigationAreaPersistenceEntity> page) {
        return new PageResult<>(
                page.getContent().stream()
                        .map(FumigationAreaPersistenceAssembler::toDomainFromPersistence)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }

    @Override
    public boolean existsById(Long id) {
        return fumigationAreaPersistenceRepository.existsById(id);
    }
}
